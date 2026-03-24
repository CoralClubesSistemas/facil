# Skill: Estandares de Git y Flujo de Trabajo - Coral Clubes

## Trigger (Cuándo usar esta skill)

Activa esta skill SIEMPRE que debas:

- Crear una nueva rama (branch) para trabajar en una funcionalidad o correccion.
- Redactar un mensaje de commit.
- Preparar o estructurar un Pull Request (PR) / Merge Request.

## 1. Principio Fundamental: "Trazabilidad y Atomizacion"

El historial de Git en Coral Clubes no es un simple respaldo de codigo, es la documentacion viva de la evolucion del
proyecto. Los commits deben ser atomicos (una sola responsabilidad por commit) y seguir un formato estandarizado legible
por maquinas y humanos.

## 2. Nomenclatura de Ramas (Branches)

Todas las ramas deben crearse a partir de `dev` (o la rama principal de integracion) y seguir la convencion:
`tipo/ticket-descripcion-corta`.
Tipos permitidos:

- `feature/`: Para nuevas funcionalidades (ej. `feature/RES-102-modal-checkin`).
- `bugfix/`: Para correccion de errores en desarrollo (ej. `bugfix/AMA-45-error-asignacion-camarista`).
- `hotfix/`: Para correccion de errores urgentes en produccion.
- `refactor/`: Para reestructuracion de codigo sin alterar funcionalidad.

## 3. Formato de Commits (Conventional Commits)

Es OBLIGATORIO utilizar la especificacion de Conventional Commits para todos los mensajes.
**Estructura:** `<tipo>(<alcance opcional>): <descripcion en imperativo>`

**Tipos estrictamente permitidos:**

- `feat`: Agrega una nueva funcionalidad (Dispara un MINOR en el versionado).
- `fix`: Soluciona un error (Dispara un PATCH en el versionado).
- `refactor`: Reescribe codigo sin cambiar su comportamiento externo.
- `perf`: Mejora el rendimiento del codigo.
- `style`: Cambios de formato (espacios, comas, punto y coma) que no afectan la logica.
- `chore`: Tareas de mantenimiento, actualizacion de dependencias o configuracion de builds.
- `docs`: Cambios exclusivos en la documentacion o READMEs.

**Ejemplo correcto:** `feat(reservaciones): agregar modal para asignacion de camaristas en tetris logico`

## 4. Estructura del Pull Request (PR)

Cuando el agente prepare un resumen para un PR, DEBE incluir:

1. **Objetivo:** Que problema resuelve o que funcionalidad agrega.
2. **Cambios principales:** Lista en viñetas de los archivos o flujos alterados.
3. **Validaciones:** Confirmacion de que el codigo compila, pasa las reglas de linting y no rompe las reglas de negocio
   descritas en las otras skills.

## 5. Prohibiciones Absolutas (Anti-Patrones)

1. **PROHIBIDO Commits Masivos (WIP):** NUNCA generes un commit llamado "WIP", "avances", "arreglos varios" o "fix bug".
   Si hay multiples cambios no relacionados, debes separarlos en commits distintos.
2. **PROHIBIDO Subir Codigo Roto o con Errores de Sintaxis:** El agente DEBE validar que el codigo escrito es correcto
   antes de sugerir el commit. No se permite comitear codigo con errores de compilacion solo para "guardar el progreso".
3. **PROHIBIDO Commits Directos a Main/Develop:** Todo el codigo debe transitar a traves de una rama secundaria y un
   Pull Request.
4. **PROHIBIDO Mezclar Idiomas:** Los mensajes de commit en Coral Clubes deben redactarse en espanol, manteniendo el
   verbo en infinitivo o imperativo (ej. "agregar", "corregir", "actualizar").