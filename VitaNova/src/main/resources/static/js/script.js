/**
 * VitaNova PHARMA ERP - Control del Sidebar (Abrir / Cerrar)
 * Botón de 3 rayas dentro del sidebar para cerrarlo.
 * Botón de 3 rayas con nombre pequeño de farmacia fuera para reabrirlo cuando está cerrado.
 */
(function () {
    const STORAGE_KEY = 'vitanova_sidebar_collapsed';

    function isMobile() {
        return window.innerWidth <= 980;
    }

    function initSidebar() {
        const body = document.body;
        const html = document.documentElement;
        const insideBtn = document.getElementById('sidebarInsideToggleBtn');
        const outsideBtn = document.getElementById('sidebarOutsideToggleBtn');

        // Overlay para dispositivos móviles
        let overlay = document.getElementById('sidebarOverlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'sidebarOverlay';
            overlay.className = 'sidebar-overlay';
            overlay.setAttribute('aria-hidden', 'true');
            document.body.appendChild(overlay);
        }

        // Sincronizar estado guardado en desktop al cargar
        const savedState = localStorage.getItem(STORAGE_KEY);
        if (!isMobile()) {
            if (savedState === 'true') {
                body.classList.add('sidebar-collapsed');
                html.classList.add('sidebar-collapsed');
            } else {
                body.classList.remove('sidebar-collapsed');
                html.classList.remove('sidebar-collapsed');
            }
        }

        function toggleSidebar() {
            if (isMobile()) {
                const isOpen = body.classList.toggle('sidebar-mobile-open');
                overlay.style.display = isOpen ? 'block' : 'none';
            } else {
                const isCollapsed = body.classList.toggle('sidebar-collapsed');
                if (isCollapsed) {
                    html.classList.add('sidebar-collapsed');
                    localStorage.setItem(STORAGE_KEY, 'true');
                } else {
                    html.classList.remove('sidebar-collapsed');
                    localStorage.setItem(STORAGE_KEY, 'false');
                }
            }
        }

        function closeSidebarMobile() {
            body.classList.remove('sidebar-mobile-open');
            overlay.style.display = 'none';
        }

        if (insideBtn) {
            insideBtn.addEventListener('click', function (e) {
                e.preventDefault();
                toggleSidebar();
            });
        }

        if (outsideBtn) {
            outsideBtn.addEventListener('click', function (e) {
                e.preventDefault();
                toggleSidebar();
            });
        }

        overlay.addEventListener('click', function () {
            closeSidebarMobile();
        });

        // Cerrar con tecla Escape en móvil
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && isMobile() && body.classList.contains('sidebar-mobile-open')) {
                closeSidebarMobile();
            }
        });

        // Responder al redimensionamiento de ventana
        window.addEventListener('resize', function () {
            if (!isMobile()) {
                body.classList.remove('sidebar-mobile-open');
                overlay.style.display = 'none';
                if (localStorage.getItem(STORAGE_KEY) === 'true') {
                    body.classList.add('sidebar-collapsed');
                    html.classList.add('sidebar-collapsed');
                } else {
                    body.classList.remove('sidebar-collapsed');
                    html.classList.remove('sidebar-collapsed');
                }
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initSidebar);
    } else {
        initSidebar();
    }
})();
