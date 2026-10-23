# ZombiEscape

Proyecto II del curso IC2101 - Programacion Orientada a Objetos.

Estudiantes:  
Fabian Cambronero Nunez  
Genesis Villegas Umana

Profesor:  
Diego Mora Rojas

II Semestre 2026

## Descripcion

ZombiEscape es un juego multijugador cooperativo desarrollado en Java utilizando programacion orientada a objetos, sockets, hilos e interfaz grafica Swing.

El juego utiliza una arquitectura cliente-servidor. El servidor mantiene el estado oficial de la partida y los clientes envian solicitudes de movimiento, chat, control de camara y disparos.

La partida se ejecuta en una sola computadora utilizando localhost en el puerto 8084.

El sistema admite entre 2 y 10 jugadores.

## Requisitos

Para ejecutar el proyecto se requiere:

- JDK 26
- Apache Maven
- Puerto local 8084 disponible
- Una computadora capaz de ejecutar varias ventanas Java

Antes de ejecutar el proyecto se recomienda comprobar que Java y Maven esten correctamente instalados.

## Compilacion

Abrir una terminal dentro de la carpeta Proyecto_ZombiEscape.

Ejecutar el comando:

mvn clean package

La compilacion debe finalizar correctamente antes de iniciar el juego.

## Ejecucion

La clase principal del proyecto es:

com.mycompany.proyecto_zombiescape.Proyecto_ZombiEscape

Al ejecutar la clase principal sin argumentos se abre un selector grafico que permite:

- Abrir servidor
- Abrir cliente

Tambien es posible ejecutar directamente las clases ClienteMain y ServidorMain desde el IDE.

## Como iniciar una partida

1. Ejecutar la aplicacion.
2. Abrir el servidor.
3. Encender el servidor desde su ventana.
4. Abrir al menos dos clientes.
5. En cada cliente ingresar un nombre.
6. Seleccionar un rol.
7. Esperar a que todos los jugadores esten conectados.
8. Iniciar la partida desde la ventana del servidor.

El servidor admite un maximo de 10 jugadores.

Para iniciar una partida se requieren al menos 2 jugadores y debe existir al menos un superviviente.

## Roles

### Superviviente

El superviviente se desplaza fisicamente por el escenario y debe llegar hasta la salida sin ser detectado por los zombis.

Controles:

Flecha arriba: mover hacia arriba  
Flecha abajo: mover hacia abajo  
Flecha izquierda: mover hacia la izquierda  
Flecha derecha: mover hacia la derecha

Cuando el superviviente entra en una casilla H queda oculto automaticamente.

Cuando sale del escondite vuelve a ser visible para los zombis.

Al llegar a la salida S queda registrado como escapado y deja de moverse.

### Francotirador

La partida permite un maximo de 2 francotiradores.

El francotirador no se desplaza fisicamente por el escenario. En su lugar controla una camara con vision limitada.

Controles:

Flechas: mover la camara  
Enter: disparar al centro de la mira

Cada francotirador dispone de:

- 10 balas por escenario
- 15 % de probabilidad de fallo
- Campo de vision limitado
- 10 puntos por cada zombi eliminado

Las balas vuelven a 10 al comenzar cada nuevo escenario.

## Zombis

Los zombis patrullan automaticamente por el escenario.

Sus principales caracteristicas son:

- Se mueven de forma periodica.
- Cambian de direccion cuando encuentran obstaculos.
- No pueden atravesar paredes.
- Detectan unicamente en la direccion hacia la que estan mirando.
- Las paredes bloquean su vision.
- No detectan supervivientes ocultos.
- Su alcance de deteccion aumenta conforme avanzan los escenarios.

## Escondites

Los escondites estan representados por el simbolo H.

Cuando un superviviente entra en un escondite queda oculto automaticamente.

Mientras este oculto, los zombis no pueden detectarlo.

Al abandonar la casilla H deja de estar oculto.

## Muerte y modo espectador

Cuando un superviviente es detectado por un zombi pasa al estado de espectador.

Un espectador:

- No puede seguir moviendose.
- Puede continuar observando la partida.
- Sigue recibiendo el estado actualizado del juego.
- Puede utilizar el chat.

La partida continua mientras existan supervivientes activos.

Si algun superviviente muere, el equipo queda marcado para derrota.

## Chat

Los jugadores pueden comunicarse mediante el chat integrado en la aplicacion.

El chat se encuentra disponible durante:

- Sala de espera
- Partida
- Modo espectador
- Pantallas de resultado

La longitud maxima de cada mensaje es de 200 caracteres.

## Escenarios

Los escenarios se encuentran dentro de la carpeta escenarios.

El proyecto incluye los escenarios base requeridos y permite agregar escenarios adicionales.

Los archivos siguen el formato:

escenario01.txt  
escenario02.txt  
escenario03.txt  
...  
escenario10.txt

Tambien pueden agregarse escenarios posteriores como:

escenario11.txt  
escenario12.txt  
escenario13.txt

Los simbolos utilizados para disenar un escenario son:

# = Pared  
. = Camino  
E = Entrada  
H = Escondite  
S = Salida

Para conocer las reglas completas de creacion de mapas se debe consultar el archivo:

Manual_Creacion_Escenarios_ZombiEscape.txt

El servidor tambien dispone de una opcion grafica para importar nuevos archivos de escenarios.

## Cronometro

Cada escenario comienza con 180 segundos.

El cronometro disminuye hasta llegar a 00:00.

Cuando el tiempo llega a cero la partida no termina automaticamente.

Los jugadores pueden continuar intentando escapar, pero ya no obtienen puntos correspondientes al tiempo restante.

## Puntuacion

Los supervivientes reciben puntos segun:

- El tiempo restante cuando escapan.
- El orden en que llegan a la salida.

Los francotiradores reciben 10 puntos por cada zombi eliminado.

Los puntos se acumulan durante los diferentes escenarios.

## Ranking

El juego mantiene un ranking de jugadores ordenado de mayor a menor cantidad de puntos.

El ranking permite comparar el desempeno de los supervivientes y francotiradores durante la partida.

Al finalizar el juego se muestra el resultado final.

## Avance entre escenarios

Cuando todos los supervivientes participantes logran escapar correctamente, la partida avanza al siguiente escenario.

Al comenzar un nuevo escenario:

- Se carga un nuevo mapa.
- Los supervivientes reciben nuevas posiciones iniciales.
- Se generan nuevos zombis.
- El cronometro vuelve a 180 segundos.
- Los francotiradores recuperan sus 10 balas.
- Los puntos acumulados se conservan.
- El alcance de deteccion de los zombis aumenta.

## Victoria

El equipo obtiene la victoria cuando completa correctamente todos los escenarios sin que el equipo haya quedado marcado para derrota.

Al finalizar se muestra el resultado de la partida y el ranking.

## Derrota

Si un superviviente muere, el equipo queda marcado para derrota.

Los jugadores restantes pueden continuar jugando y el jugador muerto permanece como espectador.

Al finalizar el escenario se muestra la derrota del equipo.

El servidor permite reintentar un escenario despues de una derrota.

## Gestion de escenarios

El servidor incluye una opcion para gestionar escenarios.

Desde esta opcion es posible importar un nuevo archivo de texto.

El sistema valida automaticamente el escenario antes de agregarlo.

Si el escenario es valido, se guarda utilizando el siguiente numero disponible.

Por ejemplo, si el ultimo escenario existente es escenario13.txt, el siguiente escenario importado sera escenario14.txt.

## Estructura principal del proyecto

Proyecto_ZombiEscape/

escenarios/  
src/main/java/Cliente/  
src/main/java/Models/  
src/main/java/Servidor/  
src/main/java/UI/  
src/main/java/com/mycompany/proyecto_zombiescape/  
src/main/resources/  
pom.xml  
nbactions.xml

Responsabilidades principales:

Cliente: comunicacion y acciones realizadas por cada jugador.

Servidor: manejo de conexiones, estado oficial y reglas de la partida.

Models: jugadores, zombis, escenarios, posiciones, mensajes y estados.

UI: componentes utilizados para construir la interfaz grafica.

## Comunicacion

El servidor utiliza:

localhost:8084

La comunicacion entre cliente y servidor se realiza mediante sockets TCP y objetos serializables.

Los clientes solicitan acciones.

El servidor valida las acciones, actualiza el estado oficial de la partida y distribuye el nuevo estado entre los jugadores conectados.

## Documentacion del proyecto

El proyecto incluye:

- Codigo completo del cliente y servidor.
- Escenarios base.
- Diagramas UML de clases.
- Manual de creacion de escenarios.
- README del proyecto.

El manual de escenarios se encuentra en:

Manual_Creacion_Escenarios_ZombiEscape.txt

## Recomendacion antes de ejecutar

Antes de realizar la demostracion final se recomienda:

1. Ejecutar mvn clean package.
2. Abrir el servidor.
3. Abrir al menos dos clientes.
4. Registrar al menos un superviviente.
5. Registrar un francotirador si se desea probar ese rol.
6. Iniciar una partida.
7. Comprobar movimiento, chat, zombis, escondites, disparos, escape y puntuacion.