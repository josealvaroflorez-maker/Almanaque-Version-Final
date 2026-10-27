# Cierre de programación — Calendario Colombia 16.0

## Entrega
Código fuente Android y workflow GitHub Actions, recursos incluidos y pruebas Java. Esta edición cierra las evoluciones solicitadas; la aceptación final depende de compilar e instalar el APK.

## Historial de versiones
| Versión | Evolución principal |
| --- | --- |
| 1 | Calendario y cálculo de festivos colombianos. |
| 2 | Vistas mensual y anual. |
| 3 | Domingos rojos e icono renovado. |
| 4 | Modo oscuro y notas por fecha. |
| 5 | Retiro del aviso del próximo festivo. |
| 6 | Icono más estilizado. |
| 7 | Respaldos e importación de notas, búsqueda. |
| 8 | Nombre Calendario Colombia y mejoras de navegación. |
| 9 | Tamaño de texto, Hoy y ajustes de compilación. |
| 10 | Filtros de notas y recordar período. |
| 11 | Icono con día actual de Colombia. |
| 12 | Recuperar última nota eliminada, exportar TXT. |
| 13 | Interfaz verde e imagen rural diferente por mes. |
| 14 | Galería y opción de ocultar fotos. |
| 15 | Foto personal por mes y restaurar paisaje incluido. |
| 16 | Compartir fechas con nota opcional y guía integrada. |

## Comprobaciones antes de dar por aceptada la versión final
- Completar todos los lotes y ejecutar el workflow del último commit; comprobar resultado exitoso y descargar el APK.
- En Samsung Galaxy A24 con Android 16, revisar vistas mensual/anual, Hoy, domingos/festivos rojos, texto grande y modo oscuro.
- Crear una nota; compartir Solo fecha y verificar que no contiene la nota; repetir incluyendo la nota. Cancelar el selector y comprobar que no se envía nada ni se guarda el borrador por compartir.
- Exportar e importar .fco, abrir TXT y recuperar la última nota eliminada.
- Elegir una foto, girar/reabrir la app, consultar galería y restaurar original. Verificar los doce meses.
- Comprobar icono diario y actualización manual, considerando caché y retrasos del lanzador.

## Conclusiones y límites
El alcance implementado reúne calendario, notas y personalización local, manteniendo los domingos y festivos en rojo y sin contador de días restantes. Las pruebas de lógica pasan; la revisión sintáctica no sustituye la compilación Android ni las pruebas en teléfono.
El usuario confirmó funcionamiento de una entrega anterior en Samsung Galaxy A24 con Android 16. No se atribuye esa confirmación a la versión 16 hasta probarla.
Los paisajes incluidos son ilustraciones generadas, no documentación fotográfica de lugares. Las fotos personales no se incluyen en el respaldo de notas. El icono depende del comportamiento del sistema. Los futuros cambios legales de festivos pueden requerir mantenimiento de las reglas. La firma debe conservarse para actualizar sin desinstalar.
