# Novedades finales
Compartir fecha desde el editor de notas, con elección explícita de incluir la nota. Guía de uso en Herramientas.

# Calendario Colombia 16.0

## Cambiar la imagen de un mes
1. Abre ☰ > Personalizar imagen de un mes.
2. Elige el mes. Los que ya tienen una foto propia aparecen identificados.
3. Pulsa Elegir una foto del teléfono y selecciónala en el selector de Android.
4. Espera el mensaje Foto guardada. La app prepara una copia optimizada; no cambia el original de la galería.
5. Abre ese mes para verla en la cabecera, la vista anual y el visor de paisajes. El cambio corresponde al mes de todos los años, no a una fecha concreta.
Si tienes las fotos del calendario ocultas, puedes ver la nueva imagen en Galería de paisajes o activar Mostrar fotos del calendario.

## Restaurar el paisaje incluido
Vuelve a Personalizar imagen de un mes, selecciona el mes y pulsa Usar paisaje original. Se solicita confirmación. Solo se quita la copia privada del mes; el archivo de tu galería no se modifica. Las imágenes rurales incluidas siguen dentro de la aplicación.

## Formatos y límites
El selector permite elegir imágenes que el dispositivo pueda decodificar. Se recomienda JPEG, PNG o WebP estático, de hasta 20 MB. La app guarda una copia JPEG optimizada y ajusta la orientación EXIF cuando está disponible. No se incluye un editor de recorte; la cabecera encuadra la imagen y el visor muestra la copia completa.
Una selección cancelada no cambia nada. Si el archivo no se puede leer, es demasiado grande o no hay memoria/espacio suficiente, se informa del error y se conserva la imagen anterior. La escritura final utiliza un archivo temporal para no sustituir el paisaje antes de terminar de preparar la copia.
La selección desde un proveedor en la nube puede necesitar conexión para abrir el original. Una vez copiada, la foto queda disponible dentro de la aplicación sin conexión.

## Copias y actualizaciones
Las fotos personales NO se incluyen en los respaldos .fco ni en el TXT: esos archivos contienen notas. Conserva las originales en tu galería. Si desinstalas, se borran las copias de la app y tendrás que elegirlas otra vez. La recuperación de la última nota eliminada solo afecta notas, no fotos.
Mismo applicationId co.calendario.festivos y claves de notas/preferencias; versionCode 16, versionName 16.0. La instalación conserva datos solo si la firma coincide con el APK anterior. La clave debug de GitHub puede variar. Exporta tus notas a .fco antes de desinstalar o reinstalar.
El icono diario conserva su funcionamiento anterior. Android puede retrasar el cambio, mantener caché o reorganizar accesos de lanzador. Abre la app tras instalar y consulta Herramientas si necesitas solicitar la actualización.

## Subir a GitHub
Descomprime el ZIP y sube los tres lotes desde Code en la raíz del repositorio, uno por vez. Arrastra el contenido de Lote-1, no la carpeta Lote-1; luego la carpeta app de Lote-2 y la de Lote-3, guardando cada carga con Commit changes. No borres app entre cargas.
Después de las tres cargas revisa la ejecución más reciente en Actions. Puedes conservar el flujo que ya compila. Para actualizarlo, pega COMPILAR-APK.yml en .github/workflows/android.yml y sube checks. El flujo incluido publica Calendario-Colombia-v16-APK. Extrae app-debug.apk del artefacto descargado.

## Validación
Se revisaron sintaxis Java, recursos, manifest y lotes. Pasaron las pruebas existentes de festivos, respaldo, búsqueda, filtros, navegación, reloj del icono y exportación de texto. No cambió la lógica de notas ni los recursos rurales originales.
No se compiló el APK ni se ejecutó la importación de fotos en Android en este entorno. Pendiente en Actions y tu Samsung Galaxy A24 con Android 16: compilación, elegir JPEG/PNG/WebP, cancelar, archivo no compatible o mayor de 20 MB, orientación, reiniciar/girar durante selección, comprobar la copia en ambas vistas y galería y restaurar el original.
Este paquete contiene el proyecto fuente, no un APK precompilado.
