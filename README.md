# Escáner de Red Local

Programa de escritorio hecho en Java para buscar y listar las computadoras o dispositivos conectados a una red local.

---

## ¿Qué hace este programa?

* **Busca en un rango de direcciones:** Escanea desde una IP inicial hasta una IP final.
* **Muestra información útil:** Muestra la IP, el nombre del equipo, si responde y el tiempo de respuesta.
* **Interfaz ágil:** La pantalla no se congela mientras realiza la búsqueda gracias a la barra de progreso.
* **Evita errores:** Valida que las direcciones IP ingresadas estén bien escritas antes de buscar.

---

## Estructura del Proyecto

El código está organizado de manera limpia en cuatro partes:

1. **Modelo (`modelo/Device.java`):** Guarda la información de cada equipo encontrado.
2. **Controlador (`controlador/EscanerRed.java`):** Realiza las pruebas de conexión y búsqueda de nombres.
3. **Vista (`vista/EscanerGui.java`):** La ventana gráfica donde el usuario interactúa.
4. **Utilidades (`utilidades/IpValidador.java`):** Comprueba que las IPs tengan un formato correcto.

---

## Cómo Ejecutarlo

1. Abrir el proyecto en **VS Code** (o tu editor preferido con Java).
2. Ejecutar el archivo `src/vista/EscanerGui.java`.
3. Ingresar el rango de IP a escanear y presionar **Escanear Rango**.