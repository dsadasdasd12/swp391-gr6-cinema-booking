/*
 * Rạp Việt CMS — Global JavaScript
 * Handles: Sidebar toggles, Dropdown interactions, and Toast notifications
 */
document.addEventListener('DOMContentLoaded', () => {
    // ── Topbar User Dropdown ──
    const userBtn = document.querySelector('.rv-topbar__user');
    const userDropdown = document.querySelector('.rv-topbar__dropdown');

    if (userBtn && userDropdown) {
        userBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            userBtn.classList.toggle('open');
            userDropdown.classList.toggle('show');
        });

        document.addEventListener('click', () => {
            userBtn.classList.remove('open');
            userDropdown.classList.remove('show');
        });
    }

    // ── Mobile Sidebar Toggle ──
    const toggleBtn = document.querySelector('.rv-topbar__toggle');
    const sidebar = document.querySelector('.rv-sidebar');

    if (toggleBtn && sidebar) {
        // Create overlay if not present
        let overlay = document.querySelector('.rv-sidebar-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'rv-sidebar-overlay';
            document.body.appendChild(overlay);
        }

        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('open');
            overlay.classList.toggle('show');
        });

        overlay.addEventListener('click', () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('show');
        });
    }

    // ── Sidebar Sub-menu Accordions ──
    const navGroups = document.querySelectorAll('.rv-nav__group');
    navGroups.forEach(group => {
        const parentLink = group.querySelector('.rv-nav__item:not(.rv-nav__sub-item)');
        if (parentLink && group.querySelector('.rv-nav__sub')) {
            parentLink.addEventListener('click', (e) => {
                // Only prevent default and toggle if it's a dropdown trigger
                const href = parentLink.getAttribute('href');
                if (!href || href === '#' || href === 'javascript:void(0)') {
                    e.preventDefault();
                    group.classList.toggle('open');
                }
            });
        }
    });

    // ── Toast Notifications ──
    // Auto dismiss toast after 4 seconds
    const toasts = document.querySelectorAll('.rv-toast');
    toasts.forEach(toast => {
        // Add close action
        const closeBtn = toast.querySelector('.rv-toast__close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                dismissToast(toast);
            });
        }

        // Set auto timeout
        setTimeout(() => {
            dismissToast(toast);
        }, 4000);
    });
});

// Function to dismiss toast with animation
function dismissToast(toast) {
    if (!toast.classList.contains('dismiss')) {
        toast.classList.add('dismiss');
        toast.addEventListener('animationend', () => {
            toast.remove();
        });
    }
}

// Global utility helper to spawn toast programmatically
window.showToast = function (title, message, type = 'info') {
    let container = document.querySelector('.rv-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'rv-toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `rv-toast rv-toast--${type}`;

    let iconClass = 'bi-info-circle-fill';
    if (type === 'success')
        iconClass = 'bi-check-circle-fill';
    if (type === 'error')
        iconClass = 'bi-exclamation-octagon-fill';
    if (type === 'warning')
        iconClass = 'bi-exclamation-triangle-fill';

    toast.innerHTML = `
    <i class="bi ${iconClass} rv-toast__icon"></i>
    <div class="rv-toast__content">
      <div class="rv-toast__title">${title}</div>
      <div class="rv-toast__message">${message}</div>
    </div>
    <button type="button" class="rv-toast__close">
      <i class="bi bi-x"></i>
    </button>
  `;

    container.appendChild(toast);

    // Attach close event
    const closeBtn = toast.querySelector('.rv-toast__close');
    closeBtn.addEventListener('click', () => {
        dismissToast(toast);
    });

    // Auto dismiss
    setTimeout(() => {
        dismissToast(toast);
    }, 4000);
};
