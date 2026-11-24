# 🎥 Guion del video — Simulador de rutas de evacuación

**Duración objetivo:** 9 a 10 minutos (el mínimo es 7). Cada integrante explica **su propia parte**.

| Bloque | Tiempo | Quién | Tema |
|---|---|---|---|
| 0 | 0:00 – 0:40 | Sebastián | Presentación del proyecto |
| 1 | 0:40 – 3:00 | Sebastián | Estructuras de datos propias |
| 2 | 3:00 – 5:20 | Oscar | Modelo del edificio (grafo) y algoritmos |
| 3 | 5:20 – 7:40 | Juan | Servicio, simulación por turnos y zonas |
| 4 | 7:40 – 9:30 | Jhoan | Interfaz gráfica, consola y pruebas |
| 5 | 9:30 – 10:00 | Jhoan | Uso de IA y cierre |

## Preparación antes de grabar
- Tener el repositorio completo (las 4 partes) compilado: `./compilar.sh`.
- Abiertos: **VS Code** con el proyecto, una **terminal** en la carpeta del proyecto y la **interfaz gráfica** (`./ejecutar.sh`).
- Pantalla a 1080p, letra del editor grande (Ctrl + `+` dos veces) y la bitácora de la GUI limpia.
- Cada uno puede grabar su bloque por separado (OBS, Zoom o Meet) y luego se unen los clips.
- En la GUI, antes de cada bloque, pulsar **“Despejar todo”** y **“Volver al estado inicial”** para empezar desde cero.

> **Convención del guion:** 🗣️ = lo que se dice (puede decirse con sus palabras) · 🖥️ = lo que se muestra en pantalla.

---

## Bloque 0 — Presentación (Sebastián, 0:00 – 0:40)

🖥️ La interfaz gráfica abierta con el edificio completo.

🗣️ *“Hola, somos Sebastián, Oscar, Juan y Jhoan, y este es nuestro Proyecto 4: un simulador de
evacuación de edificios en Java. El edificio se modela como un grafo: los nodos son habitaciones,
pasillos, escaleras y salidas, y las aristas tienen como peso el tiempo en segundos. Las conexiones se
pueden bloquear por humo o fuego. El programa calcula la ruta más segura y corta hasta la salida más
cercana y simula el desalojo por turnos. Una regla importante del proyecto es que no usamos ninguna
colección de Java: todas las estructuras las implementamos nosotros. Cada uno va a explicar la parte que desarrolló.”*

🖥️ Mostrar rápido el README en GitHub y la página de **commits**, donde se ve que cada uno subió su parte.

---

## Bloque 1 — Estructuras de datos propias (Sebastián, 0:40 – 3:00)

### 1.1 Demostrar que no se usa java.util (0:40 – 1:00)
🖥️ En la terminal escribir:
```bash
grep -rn "import java.util" src
```
🗣️ *“Este comando busca cualquier import de java.util en el código y no devuelve nada. No usamos
ArrayList, LinkedList, Stack, Queue, HashMap ni PriorityQueue. Todo está en el paquete estructuras.”*

🖥️ Abrir en VS Code la carpeta `src/evacuacion/estructuras` para que se vean los archivos.

### 1.2 Lista enlazada (1:00 – 1:25)
🖥️ Abrir `ListaEnlazada.java` y señalar los atributos `cabeza`, `ultimo` y `tamano`, y el método `agregarAlFinal`.

🗣️ *“La lista enlazada guarda la cabeza y también el último nodo, así agregar al final es O(1). La usamos
para saber quién queda en un piso, para las listas de adyacencia del grafo y para la bitácora. Tiene
métodos `filtrar`, `buscar` y `paraCada` que reciben una condición o una acción; esas interfaces también
son nuestras, `Condicion` y `Accion`, en lugar de las de java.util.function.”*

### 1.3 Cola y pila (1:25 – 1:55)
🖥️ Abrir `Cola.java` (señalar `encolar` y `desencolar`) y luego `Pila.java` (señalar `apilar`, `desapilar` y `copiar`).

🗣️ *“La cola es FIFO: el primero que llega es el primero que sale. La usamos en las puertas de las
escaleras y salidas, que son el cuello de botella de la evacuación, y también en el algoritmo BFS.
La pila es LIFO. La usamos para reconstruir las rutas: el algoritmo camina desde la salida hacia atrás
apilando nodos, y al desapilar quedan en el orden correcto. También guarda las instrucciones y el
historial de rutas. Todas las operaciones de la cola y de la pila son O(1).”*

### 1.4 Tabla hash (1:55 – 2:25)
🖥️ Abrir `TablaHash.java`: señalar la clase interna `Entrada`, el método `indiceDe` y `redimensionar`.

🗣️ *“La tabla hash resuelve colisiones por encadenamiento: cada cubeta es una cadena de entradas.
Cuando el factor de carga pasa de 0,75 duplica su tamaño y reubica las entradas, así la búsqueda sigue
siendo O(1) en promedio. La clave es el ID de la persona y el valor es la persona, con su piso, su habitación y si ya fue evacuada.”*

### 1.5 Árbol binario y montículo (2:25 – 3:00)
🖥️ Abrir `ArbolBinario.java`: señalar `insertar` (los duplicados van a la derecha), `inOrden`, `inOrdenDescendente` y `rebalancear`.
Después abrir `MonticuloMinimo.java`.

🗣️ *“El árbol binario de búsqueda guarda las zonas con su nivel de riesgo como clave. El recorrido inOrden las
da de menor a mayor riesgo y el inOrden inverso de mayor a menor, que es el orden en que se manda la brigada.
Como dos zonas pueden tener el mismo riesgo, las claves repetidas se insertan a la derecha. Además
agregamos `rebalancear`, que reconstruye el árbol tomando la mediana como raíz para que no se vuelva una lista.
Por último, el montículo de mínimos es la cola de prioridad que usa Dijkstra: inserta y extrae en O(log n).
Ahora Oscar explica cómo modelamos el edificio.”*

---

## Bloque 2 — Grafo y algoritmos (Oscar, 3:00 – 5:20)

### 2.1 El modelo del edificio (3:00 – 3:50)
🖥️ Mostrar el plano en la GUI. Pasar el mouse por encima de **ESC_B_P3** para que aparezca el tooltip con su capacidad.

🗣️ *“El edificio tiene 3 pisos y 17 ubicaciones: 6 habitaciones, 3 pasillos, 2 escaleras con un descanso
en cada piso y 2 salidas, EXIT_A y EXIT_B, con 22 conexiones en total. Modelamos cada escalera como un nodo
por piso para que bajar más pisos cueste más tiempo. Los números sobre las líneas son los segundos que se tarda en recorrer cada tramo.”*

🖥️ En VS Code abrir `modelo/Grafo.java`: señalar el arreglo `adyacencia`, la tabla `ubicacionesPorId` y el método `bloquearArista`.

🗣️ *“El grafo usa listas de adyacencia, así que ocupa memoria O(V+E), y una tabla hash para buscar una
ubicación por su ID en O(1). `bloquearArista` no borra la conexión, la marca como bloqueada; así se puede
despejar después y también sugerir rutas de emergencia. La misma arista está en la lista de los dos extremos, entonces un bloqueo afecta los dos sentidos.”*

🖥️ Abrir rápido `modelo/Ubicacion.java` y señalar el atributo `colaEspera`.

🗣️ *“Cada escalera y cada salida tienen su propia cola de personas. Por una escalera pasa una persona por turno y por una salida pasan dos.”*

### 2.2 Dijkstra (3:50 – 4:30)
🖥️ En la GUI, hacer clic en **H301**. Se resalta la ruta azul. Leer la bitácora.

🗣️ *“Al hacer clic en la habitación 301, Dijkstra encuentra la ruta más rápida: pasillo, escalera B,
bajar dos pisos y salida B, en 31 segundos. Las instrucciones salen de una pila: el tope es el primer paso.”*

🖥️ Abrir `algoritmos/Dijkstra.java`, método `calcularDistancias`, y señalar el arreglo `cerrado` y la línea `continue`.

🗣️ *“Usamos el montículo propio con eliminación perezosa: si un nodo ya fue cerrado, su entrada vieja
se ignora. Calculamos la distancia a todas las salidas en una sola ejecución y elegimos la menor; eso es
Dijkstra a múltiples salidas. La complejidad es O((V+E) log V). Las conexiones bloqueadas se saltan, por eso la ruta es la más segura.”*

### 2.3 Comparar BFS con Dijkstra (4:30 – 5:00)
🖥️ En el combo **Origen** elegir **H101 · Habitación 101** y pulsar **“Comparar BFS vs Dijkstra”**.

🗣️ *“BFS recorre por niveles con nuestra cola y encuentra la salida con menos pasos: desde la 101
sale directo por el lobby en 2 tramos, pero tarda 22 segundos. Dijkstra usa 3 tramos, pasando por la escalera A,
y tarda solo 13 segundos. En una evacuación importa el tiempo y no la cantidad de pasos, por eso el sistema usa Dijkstra.”*

### 2.4 Bloqueos y caso borde (5:00 – 5:20)
🖥️ Hacer clic sobre la línea **PAS3 — ESC_B_P3**: se vuelve roja con la palabra “HUMO”. Clic en **H301**: ahora la ruta va por la escalera A (33 s).
Después clic en la línea **H301 — PAS3** y otra vez en **H301**: aparece la ruta **naranja** y el mensaje “No existe ruta libre”.

🗣️ *“Si hay humo entre el pasillo y la escalera B, la ruta cambia sola a la escalera A. Y si la habitación
queda aislada, el sistema avisa que no hay ruta libre y sugiere una ruta de emergencia: Dijkstra multiplica
por diez el costo de cruzar el humo, así encuentra el camino que menos humo atraviesa. Esa ruta es solo para usarla con la brigada.”*

🖥️ Pulsar **“Despejar todo”**.

🗣️ *“Juan va a explicar cómo se usa todo esto en la simulación.”*

---

## Bloque 3 — Servicio, simulación y zonas (Juan, 5:20 – 7:40)

### 3.1 La fachada EvacuacionService (5:20 – 5:50)
🖥️ Abrir `servicio/EvacuacionService.java` y mostrar los atributos (`personasPorId`, `personas`, `historialRutas`, `gestorZonas`, `simulador`) y el método `evacuarDesde`.

🗣️ *“EvacuacionService es una fachada: tanto la interfaz gráfica como la consola llaman solo a esta clase,
así la lógica no depende de la pantalla. Aquí se juntan todas las estructuras: la tabla hash de personas,
la lista enlazada en orden de registro, la pila con el historial de rutas, el árbol de zonas y el grafo.
`evacuarDesde` calcula la ruta con Dijkstra y, si no la encuentra, arma la ruta de emergencia.”*

### 3.2 Simulación por turnos (5:50 – 6:50)
🖥️ En la GUI pulsar **“Iniciar / reiniciar”**. **Antes de avanzar**, hacer clic en la línea **H102 — PAS1**: la bolita **P08** (Pedro) se pone roja.

🗣️ *“Si aparece humo en plena evacuación, se recalculan las rutas de todos desde donde están. Pedro quedó
encerrado en la 102, así que su estado pasa a ATRAPADO y espera a la brigada.”*

🖥️ Pulsar **“Siguiente turno”** 3 veces, despacio. Señalar con el mouse las bolitas que aparecen con la palabra **“cola”** debajo de las escaleras.

🗣️ *“En cada turno pasan dos cosas. Primero, cada escalera y cada salida dejan pasar a las personas de su
cola en orden FIFO, una por turno en las escaleras y dos en las salidas. Después, las demás personas avanzan
un nodo por su ruta, sacándolo de su pila. Si el siguiente nodo es una escalera o una salida, la persona se forma en la cola.
Aquí se ve la fila en las escaleras: ese es el cuello de botella.”*

🖥️ Hacer clic otra vez en la línea **H102 — PAS1** para despejarla: Pedro vuelve a su color.

🗣️ *“Cuando se despeja el humo, Pedro recupera su ruta y sigue evacuando.”*

🖥️ Pulsar **“▶ Automático”** y dejarlo correr hasta el final. Mostrar el **punto de encuentro** y la barra superior con **8/8**.

🗣️ *“Cuando alguien cruza una salida, la tabla hash lo marca como evacuado y queda anotado el turno en que salió. La simulación termina cuando ya no queda nadie adentro.”*

🖥️ Abrir `servicio/SimuladorEvacuacion.java`, método `avanzarTurno` → `procesarColas` → `moverPersonasLibres`.

### 3.3 Zonas por riesgo (6:50 – 7:20)
🖥️ Pulsar **“Volver al estado inicial”**. Bloquear las líneas **H301 — PAS3** y **PAS3 — ESC_A_P3**. Pulsar **“Zonas por riesgo (árbol)”**.

🗣️ *“El riesgo de cada zona es su riesgo base, más 25 puntos por cada conexión con humo, más 5 por cada
persona que sigue adentro. El piso 3 tiene base 30, dos conexiones con humo y tres personas: 30 más 50 más 15, o sea 95.
Las zonas se guardan en el árbol binario; el inOrden da el reporte de menor a mayor riesgo y el inOrden inverso da
la prioridad de la brigada, que empieza por el piso 3. Abajo se ve el dibujo del árbol, que ya está balanceado.”*

### 3.4 Plano ASCII y exportar (7:20 – 7:40)
🖥️ Pulsar **“Plano ASCII (matriz)”** y luego **“Exportar plan .txt…”**. Guardar y abrir el archivo en VS Code.

🗣️ *“Además del grafo tenemos una matriz de pisos por columnas para dibujar el edificio en ASCII, con
las personas, las colas y el humo. Y el plan completo se exporta a un .txt: el plano, la lista de adyacencia,
la ruta e instrucciones de cada habitación, las personas, las zonas y el estado de la simulación.
Jhoan va a mostrar las interfaces y las pruebas.”*

🖥️ Pulsar **“Despejar todo”** y **“Volver al estado inicial”**.

---

## Bloque 4 — Interfaz gráfica, consola y pruebas (Jhoan, 7:40 – 9:30)

### 4.1 Interfaz gráfica (7:40 – 8:20)
🖥️ Abrir `vista/gui/PanelEdificio.java` y señalar `paintComponent`, `formaDe` y `manejarClic`.

🗣️ *“La interfaz está hecha con Swing. PanelEdificio dibuja los pisos, las conexiones, las personas y las colas. Las conexiones
largas del mismo piso se dibujan como curvas para que no pasen por encima de otros nodos. Con un clic se detecta
si tocaste un nodo o una conexión: en un nodo calcula la ruta y en una conexión la bloquea o la despeja.”*

🖥️ En la GUI mostrar **“Buscar por ID…”** → escribir `P03`. Después **“¿Quién queda en un piso?”** → 3. Después **“Registrar persona…”** → ID `P09`, nombre `Prueba`, ubicación `H202`.

🗣️ *“Buscar por ID usa la tabla hash: muestra en O(1) el piso, la habitación y si la persona fue evacuada.
‘Quién queda en el piso’ recorre la lista enlazada y filtra. Y aquí registramos una persona nueva, que también va a la tabla hash.”*

🖥️ Pulsar **“Historial de rutas (pila)”**.

🗣️ *“El historial es una pila: arriba aparece la ruta que se calculó de último.”*

### 4.2 Consola (8:20 – 8:50)
🖥️ Cerrar la GUI. En la terminal escribir `./ejecutar.sh --consola`. Elegir la opción **1** (plano), luego la **2** con `H302` y después la **12** (simular hasta el final).

🗣️ *“También hay un menú de consola con las mismas funciones. Por ejemplo, la habitación 302 sale por una
puerta de servicio directo a la escalera B en 29 segundos, sin pasar por el pasillo. Y con la opción 12 se simula toda la evacuación.”*

### 4.3 Pruebas (8:50 – 9:30)
🖥️ Escribir `0` para salir. Ejecutar `./probar.sh`. Mostrar el final: **“49 aprobadas, 0 fallidas”**. Abrir `test/evacuacion/pruebas/PruebasProyecto.java`.

🗣️ *“Escribimos 49 pruebas automáticas sin librerías externas. Comprueban que la cola sea FIFO y la pila
LIFO, que la tabla hash funcione con mil claves, el árbol con claves repetidas, los tiempos exactos de
Dijkstra y BFS, el caso sin ruta, la simulación completa y el bloqueo a mitad de la simulación. Todas pasan.”*

---

## Bloque 5 — Uso de IA y cierre (Jhoan, 9:30 – 10:00)

🖥️ Mostrar en el README la sección **“Uso de IA (Claude)”**.

🗣️ *“Como recomendó el enunciado, usamos Claude como asistente después de la fase 1: para discutir cómo
modelar el edificio como grafo, para revisar Dijkstra y la reconstrucción de rutas con pila, para construir
la interfaz gráfica y para escribir las pruebas. Todo el código lo revisamos, lo ejecutamos y lo probamos nosotros.
En conclusión, cada estructura resuelve un problema concreto de la evacuación: la cola el cuello de botella, la pila las
rutas, la hash la búsqueda de personas, el árbol la prioridad de la brigada y el grafo los caminos.
Gracias por su atención.”*

---

## ✅ Checklist antes de subir a YouTube
- [ ] Dura más de 7 minutos.
- [ ] Se escucha bien a los 4 y cada uno explica su parte.
- [ ] Se ven: grep sin java.util, ruta Dijkstra, comparación BFS, bloqueo y caso borde, simulación con colas, árbol de zonas, ASCII, exportar .txt, consola y pruebas.
- [ ] Subirlo como **“No listado”** o **“Público”** (no “Privado”, porque así el profesor no lo puede ver).
- [ ] Pegar el enlace en el README y en la entrega.
