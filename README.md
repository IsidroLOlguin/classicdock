<div align="center">
  <h1>ClassicDock</h1>
  Fork de Smart Dock con el dock y la barra de apps al estilo del DeX clásico, inspirado en YoukiDEX
</div>

ClassicDock parte de [Smart Dock](https://github.com/axel358/smartdock) de axel358 y toma como referencia visual a [YoukiDEX](https://github.com/mrYouki/YoukiDex-Android-Desktop) de mrYouki (archivado). Solo cambia el aspecto del dock y de la barra de apps; el resto de la lógica es la de Smart Dock.

Se instala junto a Smart Dock original (`dev.isidro.classicdock`), sin conflicto.

## Licencia y créditos
- GPL-3.0, igual que ambos proyectos. Ver [LICENSE](LICENSE).
- Atribución y procedencia de cualquier código portado: [NOTICE.md](NOTICE.md).
- Colaboradores de Smart Dock: [Contributors.md](Contributors.md).

## Uso

### Permisos restringidos
En algunos dispositivos los permisos de Accesibilidad y Notificaciones no están disponibles. Ve a Ajustes > Apps > ClassicDock > menú de tres puntos > Permitir ajustes restringidos.

### Ajustes seguros
Ejecuta en un shell de adb o root:
```
pm grant dev.isidro.classicdock android.permission.WRITE_SECURE_SETTINGS
```

Con `scripts/deploy.sh` (cuando exista) se compila, instala y otorga el permiso en un solo comando.

### Ocultar la barra de navegación
[Ver cómo ocultar la barra de navegación](HideNav.md)

## Desarrollo
Ramas: `feature/*` → `dev` → `master`. Para traer cambios de Smart Dock, ver `docs/maintenance.md`. Plan y requisitos en `docs/sdlc/`.
