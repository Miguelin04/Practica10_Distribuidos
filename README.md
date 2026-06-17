# Práctica 10: Sistemas Distribuidos - Algoritmos de Elección y Exclusión Mutua

Este repositorio contiene la implementación de la Práctica 10 para la asignatura de Sistemas Distribuidos. El proyecto consiste en una simulación basada en nodos (representando nodos de hospitales) que se comunican de forma distribuida para gestionar recursos compartidos y elegir a un coordinador.

## Características Principales

* **Algoritmo de Elección Bully**: Implementación del algoritmo Bully para la elección de un líder o coordinador entre los nodos distribuidos. En caso de que el coordinador actual falle, los nodos detectan la falla y se inicia un proceso de elección donde el nodo con el ID más alto asume el liderazgo.
* **Exclusión Mutua Distribuida**: Gestión del acceso a recursos compartidos en la red de nodos del hospital, garantizando que no se produzcan condiciones de carrera ni bloqueos.
* **Sincronización y Ordenamiento**: Uso de relojes vectoriales (Lamport) y sincronización de tiempo (algoritmo de Cristian) para mantener un orden causal y cronológico en los eventos y mensajes del sistema distribuido.
* **Simulación de 5 Nodos**: La red consta de 5 nodos independientes que se comunican entre sí para intercambiar mensajes de estado, solicitudes de recursos y mensajes de elección.

## Tecnologías Utilizadas

* **Backend**: Java (Spring Boot) - Lógica de los algoritmos distribuidos (Bully, Lamport, etc.).
* **Frontend**: Interfaz gráfica para visualizar el estado del clúster, los mensajes intercambiados y el líder actual.

## Ejecución

1. Iniciar los nodos del backend (puertos configurados para cada instancia de nodo).
2. Levantar el frontend para visualizar la red y simular la caída/recuperación del líder.