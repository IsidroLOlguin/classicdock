# PRD — ClassicDock

Fork de Smart Dock (GPL-3.0) cuyo dock y barra de apps se parecen al DeX clásico de Samsung, tomando como referencia visual YoukiDEX.

## Metas
1. El dock se ve como una barra flotante translúcida centrada, al estilo del DeX clásico.
2. Se conserva la arquitectura y estabilidad de Smart Dock; solo cambia lo visual del dock y la barra de apps.
3. Instalable junto a Smart Dock original (`dev.isidro.classicdock`).

## No-metas
1. Decoración de ventanas de otras apps (la dibuja el desktop windowing de One UI 8).
2. Wallpapers en video, Workshop, plugins, tweaks de rendimiento, multiusuario, motor en Rust.
3. Monitor CPU/RAM, panel con reproductor y mosaicos, Material You claro, miniaturas de ventanas, dock que se encoge al contenido.
4. Bandeja y paneles más allá de la píldora de estado (van después de v1).

## Usuarios y jobs-to-be-done
| Persona | Job-to-be-done |
|---|---|
| Isidro, dueño de una Galaxy Tab S9 Ultra con One UI 8 | Tener una barra de tareas con el aspecto del DeX clásico que Samsung eliminó |

## Requisitos funcionales
| ID | Requisito | Prioridad (MoSCoW) |
|---|---|---|
| FR-01 | Identidad propia: applicationId `dev.isidro.classicdock`, nombre «ClassicDock», README y NOTICE.md con atribución | Must |
| FR-02 | Ícono de 44dp y celda de la fila derivada de un solo valor (sin el 52dp fijo) | Must |
| FR-03 | Dock flotante: margen de 8dp, esquinas de 20dp, centrado; paneles, menús y popups anclados correctamente | Must |
| FR-04 | Modo de tema `fully_transparent` (alpha 0) y opacidad coherente en dock, píldoras y panel | Must |
| FR-05 | Indicador de app abierta: barra 16x3dp si es la activa, punto 4x4dp si está en segundo plano, color `#CCFFFFFF` | Must |
| FR-06 | Bandeja derecha: wifi, volumen, bluetooth y batería dentro de una píldora de 14dp; reloj fuera de la píldora | Should |
| FR-09 | Barra propia con minimizar, maximizar y cerrar visibles en toda ventana, incluso a pantalla completa | Should |
| FR-07 | Animación de mostrar/ocultar con fade y escala 0.90 a 180 ms | Should |
| FR-08 | `scripts/deploy.sh`: build, instalación y permisos en un comando | Must |

## Requisitos no funcionales (medibles)
| ID | Requisito | Métrica objetivo |
|---|---|---|
| NFR-01 | El build no se rompe | `./gradlew assembleDebug` sin errores en cada story |
| NFR-02 | Compatibilidad con la lógica existente | Sin Shizuku (UsageStats) el indicador sigue tolerando `tasks[0].id == -1` |
| NFR-03 | Licencia | Código portado de YoukiDEX conserva encabezados y se registra en NOTICE.md |
| NFR-04 | Verificable en tablet | Cada story trae captura en `docs/progress/` |

## Supuestos
- La tablet se conecta por ADB inalámbrico; los permisos restringidos y el servicio de accesibilidad los activa Isidro a mano.

## Preguntas abiertas
- [PENDIENTE] Capturas del DeX clásico en `docs/reference/dex-classic/` para afinar el parecido (no bloquea).

## Métricas de éxito
| Meta | Métrica |
|---|---|
| 1 | Isidro confirma en la tablet que el aspecto se acerca al DeX clásico |
| 2 | Cero regresiones: panel rápido, notificaciones y menús siguen apareciendo en su lugar |

## Historial de cambios
| Fecha | Cambio | Motivo | Sprint origen |
|---|---|---|---|
| 2026-10-06 | Versión inicial | Alcance «Mínimo DeX clásico» | n/a |
