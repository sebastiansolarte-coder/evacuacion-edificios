# 🤝 Guía de colaboración — cómo sube cada uno su parte

Repositorio: **https://github.com/sebastiansolarte-coder/evacuacion-edificios**

| Parte | Integrante | Qué sube | Depende de |
|---|---|---|---|
| 1 | Sebastián (dueño del repo) | `src/evacuacion/estructuras/`, README, docs, scripts | — |
| 2 | Oscar | `src/evacuacion/modelo/`, `src/evacuacion/algoritmos/` | Parte 1 |
| 3 | Juan | `src/evacuacion/servicio/`, `src/evacuacion/datos/` | Partes 1 y 2 |
| 4 | Jhoan | `src/evacuacion/vista/`, `src/evacuacion/App.java`, `test/` | Partes 1, 2 y 3 |

**Orden recomendado para subir:** Oscar → Juan → Jhoan. Si alguien sube antes de su turno no pasa
nada grave; solo que el proyecto no compila completo hasta que estén las 4 partes.

---

## 0. Lo que necesita cada uno (una sola vez)

1. Una cuenta de GitHub. Hay que enviarle el **nombre de usuario** a Sebastián.
2. Tener **Git** instalado: <https://git-scm.com/downloads> (en Windows también trae “Git Bash”).
3. Tener **JDK 17 o superior** para compilar (opcional pero recomendado).
4. Configurar el nombre y correo (los mismos de la cuenta de GitHub, para que los commits salgan a su nombre):
   ```bash
   git config --global user.name "Tu Nombre"
   git config --global user.email "tu-correo-de-github@ejemplo.com"
   ```

---

## 1. Sebastián: dar permiso a los compañeros

1. Entrar a <https://github.com/sebastiansolarte-coder/evacuacion-edificios>.
2. **Settings** → **Collaborators** (en el menú izquierdo, sección *Access*).
3. **Add people** → escribir el usuario de GitHub del compañero → **Add to repository**.
4. Repetir con Oscar, Juan y Jhoan.

A cada uno le llega un **correo de invitación** (también aparece en <https://github.com/notifications>).
**Tienen que aceptarla**; hasta que no la acepten, GitHub no les deja subir nada (`git push` da error 403).

> Como el repositorio es **privado**, el profesor tampoco lo puede ver. Cuando esté completo:
> agregar al profesor como colaborador, o hacerlo público en **Settings → General → Danger Zone →
> Change repository visibility**.

---

## 2. Cada compañero: subir su parte

### 2.1 Clonar el repositorio (descargarlo)
```bash
git clone https://github.com/sebastiansolarte-coder/evacuacion-edificios.git
cd evacuacion-edificios
```
La primera vez, Git pide iniciar sesión en GitHub (se abre el navegador o una ventana). Hay que
entrar con **su propia cuenta**, la que se invitó.

### 2.2 Copiar los archivos del zip
Descomprimir el zip que les mandó Sebastián **dentro de la carpeta `evacuacion-edificios`**.
El zip ya trae las rutas correctas (`src/evacuacion/...`), así que los archivos caen en su lugar.
Debe quedar así (ejemplo de Oscar):
```
evacuacion-edificios/
└── src/evacuacion/
    ├── estructuras/   ← ya estaba (Sebastián)
    ├── modelo/        ← nuevo (Oscar)
    └── algoritmos/    ← nuevo (Oscar)
```

### 2.3 (Opcional) Comprobar que compila
```bash
./compilar.sh          # Linux / Mac / Git Bash
compilar.bat           # Windows (cmd)
```
Debe terminar en `OK -> evacuacion.jar generado`.

### 2.4 Traer lo último que hayan subido los demás
Siempre antes de subir:
```bash
git pull
```

### 2.5 Registrar y subir los cambios
```bash
git add .
git status                         # revisar que aparezcan SOLO tus archivos
git commit -m "Parte 2 (Oscar): modelo del edificio y algoritmos Dijkstra/BFS"
git push
```
Mensajes de commit sugeridos:
- Oscar: `Parte 2 (Oscar): modelo del edificio y algoritmos Dijkstra/BFS`
- Juan: `Parte 3 (Juan): servicio de evacuación, simulación y edificio de ejemplo`
- Jhoan: `Parte 4 (Jhoan): interfaz gráfica, consola y pruebas`

¡Listo! El commit queda con su nombre en
<https://github.com/sebastiansolarte-coder/evacuacion-edificios/commits/main>, que es lo que revisa el profesor.

---

## 3. Problemas comunes

| Mensaje | Qué significa | Solución |
|---|---|---|
| `Permission denied` / `403` | No has aceptado la invitación o iniciaste sesión con otra cuenta | Aceptar la invitación; en Windows borrar la credencial vieja en *Administrador de credenciales → github.com* |
| `rejected ... fetch first` | Alguien subió algo antes que tú | `git pull` y luego `git push` otra vez |
| `Please tell me who you are` | Falta configurar nombre/correo | Paso 0.4 |
| Se abre un editor raro al hacer `git pull` | Git pide mensaje para unir cambios | Guardar y cerrar (en vim: escribir `:wq` y Enter) |
| `CONFLICT` | Dos personas cambiaron el mismo archivo | No debería pasar si cada uno solo agrega sus carpetas. Si pasa, avisar a Sebastián antes de seguir |

**Regla de oro:** cada uno solo agrega **sus** carpetas. No editar archivos de otra parte sin avisar.

---

## 4. Verificación final (cuando estén las 4 partes)
```bash
git pull
./compilar.sh && ./ejecutar.sh     # abre la interfaz gráfica
./probar.sh                        # debe decir: 49 aprobadas, 0 fallidas
```
