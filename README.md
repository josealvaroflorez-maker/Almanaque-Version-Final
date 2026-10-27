# Calendario Colombia 16.0 — Edición final

Última evolución programada: compartir una fecha y guía de uso integrada.

## Novedades
Toca un día en la vista mensual y pulsa Compartir esta fecha. El selector de Android permite elegir la aplicación de destino. Si el editor contiene una nota, puedes compartir solo la fecha o incluir el texto actual; también puedes cancelar. Compartir no guarda la edición ni envía automáticamente. El texto incluye todos los festivos que coincidan y sus traslados.
Herramientas > Guía de uso reúne instrucciones sobre calendario, notas, fotos, respaldos e icono diario.

Se mantienen vistas mensual y anual, domingos y festivos rojos, imágenes rurales y fotos personales por mes, modo oscuro, tamaños de texto, búsqueda, notas, recuperación de última nota, respaldo .fco, TXT e icono del día de Colombia. Sin cuenta regresiva al próximo festivo.

## Instalación y compilación
Este ZIP contiene código fuente; no es un APK precompilado.
Sube los tres lotes como indica INSTRUCCIONES-DE-CARGA.txt. El workflow va en `.github/workflows/android.yml` (no `.gihub`). Puedes pegar COMPILAR-APK.yml en ese archivo.
Tras terminar las tres cargas, ejecuta el flujo más reciente en Actions. El artefacto se llama Calendario-Colombia-v16-APK; descarga y extrae app-debug.apk.
Requisitos del proyecto: JDK 17, Gradle 8.9, SDK 35, AGP 8.7.3; Android mínimo 8.0. Sin Gradle Wrapper.

## Datos
Exporta tus notas a .fco antes de reinstalar. Los respaldos .fco y TXT no incluyen fotos ni ajustes. Conserva las fotos originales.
Se mantiene co.calendario.festivos; versionCode 16 y versionName 16.0. Actualizar conserva datos si coincide la firma. Las claves debug de GitHub pueden variar: si necesitas desinstalar, respalda antes.
El icono puede actualizarse con retraso según Android y el lanzador. Abre la app después de instalar.

## Validación y cierre
Pasaron siete grupos de pruebas Java: festivos, respaldo/búsqueda, navegación, filtros, reloj diario, TXT y compartir fechas. Se comprobó la sintaxis Java de la interfaz. El ZIP y la reconstrucción de los lotes se verificaron.
No se compiló el APK ni se ejecutó esta versión en Android en este entorno. Tu confirmación previa de funcionamiento en Samsung Galaxy A24 con Android 16 corresponde a una versión anterior; falta validar la 16.
Consulta CIERRE-TECNICO.md para alcance y comprobaciones finales.
