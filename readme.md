# Jaime Adonay Jiménez Clímaco 00081622

## Indicaciones

Recientemente, se utilizó AI para crear un sistema de gestion de una biblioteca, el cual ha generado varios errores, su trabajo es arreglarlo. Dado el siguiente caso de uso, explique y/o resuelva cada problema según se le pida.

---

## Consideraciones

La libreria crea automaticamente un correo con los nombres de la persona

---

## Problemas

### 1. Filtro por autor y género (10%)

QA ha reportado que el endpoint para obtener los libros puede filtrar por **autor** y por **género**, o por cualquiera de los dos de manera individual.

Actualmente:

- Filtrar únicamente por autor funciona correctamente.
- Filtrar únicamente por género funciona correctamente.
- Filtrar por **autor y género al mismo tiempo** provoca que el servidor falle.

**Instrucción:** Explique la causa del problema y resuélvalo.

**Causa:** BookRepository definía findByAuthorAndGenre(String author, String genre), es decir, ambos parámetros como `String`, aunque el campo `genre` de la entidad `Book` es un enum

**Solución:** Se cambió la firma del repositorio a `findByAuthorAndGenre(String author, Genre genre)` y se corrigió el orden de los argumentos en el servicio, convirtiendo el género recibido a `Genre` (`Genre.valueOf(genre.toUpperCase())`) antes de la consulta.

---

### 2. Error al volver a prestar un libro (10%)

Un usuario reportó que al pedir prestado el libro **The Selfish Gene**, devolverlo e intentar pedirlo prestado nuevamente, el servidor falla.

**Instrucción:** Explique la causa del problema y resuélvalo.

**Causa:** En MovementService, el método `returnBook` (antes createMovement con MovementType.RETURN) incrementaba `availableCount` al devolver el libro, pero nunca volvía a poner `available = true`. Como borrowBook valida if (!book.isAvailable()) antes de permitir un nuevo préstamo, el libro quedaba marcado como no disponible para siempre después del primer préstamo

**Solución:** Se separaron borrowBook y returnBook, y en ambos se recalcula available en función del availableCount actualizado (book.setAvailable(book.getAvailableCount() > 0) al prestar, book.setAvailable(true) al devolver), de modo que el estado de disponibilidad siempre sea consistente con el conteo real de copias.

---

### 3. Cantidad de libros por género (10%)

Existe un endpoint que devuelve la cantidad de libros disponibles por género. Sin embargo, actualmente dicho endpoint falla.

**Instrucción:** Explique la causa del problema y resuélvalo.

**Causa:** BookService.getGenresAvailable recorría todos los libros con bookRepository.findAll() y ejecutaba `book.getGenre().name()` para agruparlos. El libro "The Art of War" tiene genre = NULL en data.sql, por lo que getGenre() devolvía null y .name() lanzaba NullPointerException, tumbando el endpoint.

**Solución:** Se movió el conteo a una consulta JPQL (BookRepository.countByGenre) que agrupa directamente en la base de datos con GROUP BY b.genre y construye GenreCountDto mediante SELECT NEW.

---

### 4. Error al consultar un libro por ID (10%)

Un miembro del equipo de frontend reporta que la siguiente llamada falla:

```http
GET /books?id=ed16ed1e-7017-4697-a08a-d28c09a74acf
```

**Instrucción:** Explique la causa del problema.

**Causa:** No es un error del servidor, sino un uso incorrecto del contrato de la API por parte del equipo de frontend. `BookController` expone la búsqueda por ID como **path variable**: `GET /books/{id}`. El endpoint `GET /books` (sin `/{id}`) solo reconoce los query params `author` y `genre`

---

### 5. Error al crear un libro (10%)

QA ha reportado que el siguiente payload enviado al endpoint `POST /books` provoca un error:

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "genre": "classic",
  "isbn": "978-0132350884",
  "available": true,
  "availableCount": 5
}
```

**Instrucción:** Explique la causa del problema.

**Causa:** BookService.createBook construye el libro sin normalizar mayúsculas/minúsculas. Genre.valueOf(...) es sensible a mayúsculas y los valores del enum están en mayúscula (`CLASSIC`, `CRIME`, etc.). 

---

### 6. Devolución de libros no prestados (20%)

QA ha reportado que un usuario es capaz de devolver libros que nunca ha solicitado en préstamo.

**Instrucción:**

- Confirme si este comportamiento es realmente posible.
- Si es posible, explique la causa y resuelva el problema.
- Si no es posible, explique por qué, haciendo referencia al código correspondiente.

**Sí es posible.** El método returnBook original solo verificaba que el Lector y el Book existieran (por email e ISBN respectivamente) e incrementaba availableCount, sin comprobar que ese lector tuviera realmente un préstamo (BORROWING) activo de ese libro. Cualquier lector registrado podía "devolver" cualquier libro sin haberlo pedido antes.

**Solución:** En MovementRepository se agregó findTopByLectorAndBookOrderByTimestampDesc(Lector lector, Book book), que obtiene el último movimiento registrado entre ese lector y ese libro. MovementService.returnBook` ahora:
1. Busca ese último movimiento; si no existe, lanza RuntimeException("Lector has never borrowed this book").
2. Si existe pero su tipo no es BORROWING (es decir, el último movimiento ya fue una devolución), lanza RuntimeException("Lector does not currently have this book borrowed").
3. Solo si el último movimiento fue un préstamo activo, procede a incrementar availableCount, marcar available = true y registrar el movimiento de tipo RETURN.

---