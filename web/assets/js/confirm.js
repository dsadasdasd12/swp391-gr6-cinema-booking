/*
 * Rap Viet CMS — Confirmation Modal JavaScript
 * Vietnamese UI strings use Unicode escapes so encoding stays correct when served.
 */
(function () {
    var LABEL_CANCEL = 'H\u1ee7y b\u1ecf';
    var LABEL_CONFIRM = 'X\u00e1c nh\u1eadn';
    var LABEL_AGREE = '\u0110\u1ed3ng \u00fd';
    var LABEL_DEFAULT_TITLE = 'X\u00e1c nh\u1eadn h\u00e0nh \u0111\u1ed9ng';
    var LABEL_DEFAULT_MESSAGE = 'B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n th\u1ef1c hi\u1ec7n h\u00e0nh \u0111\u1ed9ng n\u00e0y?';
    var LABEL_BODY_PLACEHOLDER = 'N\u1ed9i dung x\u00e1c nh\u1eadn...';

    document.addEventListener('DOMContentLoaded', function () {
        document.addEventListener('click', function (e) {
            var trigger = e.target.closest('[data-confirm]');
            if (!trigger)
                return;

            e.preventDefault();

            var title = trigger.getAttribute('data-confirm-title') || LABEL_DEFAULT_TITLE;
            var message = trigger.getAttribute('data-confirm-message') || LABEL_DEFAULT_MESSAGE;
            var type = trigger.getAttribute('data-confirm-type') || 'warning';
            var confirmText = trigger.getAttribute('data-confirm-text') || LABEL_AGREE;
            var cancelText = trigger.getAttribute('data-confirm-cancel') || LABEL_CANCEL;

            showConfirmModal({
                title: title,
                message: message,
                type: type,
                confirmText: confirmText,
                cancelText: cancelText,
                onConfirm: function () {
                    if (trigger.tagName === 'A' && trigger.getAttribute('href')) {
                        window.location.href = trigger.getAttribute('href');
                    } else if (trigger.tagName === 'BUTTON' && trigger.type === 'submit') {
                        var form = trigger.closest('form');
                        if (form) {
                            if (trigger.name) {
                                var hiddenInput = document.createElement('input');
                                hiddenInput.type = 'hidden';
                                hiddenInput.name = trigger.name;
                                hiddenInput.value = trigger.value || '1';
                                form.appendChild(hiddenInput);
                            }
                            form.submit();
                        }
                    } else {
                        var nestedForm = trigger.closest('form');
                        if (nestedForm)
                            nestedForm.submit();
                    }
                }
            });
        });
    });

    window.showConfirmModal = function (options) {
        var modalOverlay = document.getElementById('rv-confirm-modal-overlay');

        if (!modalOverlay) {
            modalOverlay = document.createElement('div');
            modalOverlay.id = 'rv-confirm-modal-overlay';
            modalOverlay.className = 'rv-modal-overlay';
            modalOverlay.innerHTML =
                    '<div class="rv-modal">' +
                    '<div class="rv-modal__header">' +
                    '<div class="rv-modal__icon" id="rv-confirm-icon">' +
                    '<i class="bi bi-exclamation-triangle"></i>' +
                    '</div>' +
                    '<h3 class="rv-modal__title" id="rv-confirm-title">' + LABEL_CONFIRM + '</h3>' +
                    '</div>' +
                    '<div class="rv-modal__body" id="rv-confirm-message">' + LABEL_BODY_PLACEHOLDER + '</div>' +
                    '<div class="rv-modal__footer">' +
                    '<button type="button" class="rv-btn rv-btn--ghost rv-btn--sm" id="rv-confirm-btn-cancel">' + LABEL_CANCEL + '</button>' +
                    '<button type="button" class="rv-btn rv-btn--sm" id="rv-confirm-btn-ok">' + LABEL_CONFIRM + '</button>' +
                    '</div>' +
                    '</div>';
            document.body.appendChild(modalOverlay);
        }

        var iconDiv = modalOverlay.querySelector('#rv-confirm-icon');
        var titleH = modalOverlay.querySelector('#rv-confirm-title');
        var messageDiv = modalOverlay.querySelector('#rv-confirm-message');
        var btnCancel = modalOverlay.querySelector('#rv-confirm-btn-cancel');
        var btnOk = modalOverlay.querySelector('#rv-confirm-btn-ok');

        iconDiv.className = 'rv-modal__icon ' + (options.type || 'warning');
        var iconClass = 'bi-exclamation-triangle-fill';
        if (options.type === 'danger')
            iconClass = 'bi-trash-fill';
        if (options.type === 'success')
            iconClass = 'bi-check-circle-fill';
        if (options.type === 'info')
            iconClass = 'bi-info-circle-fill';
        iconDiv.innerHTML = '<i class="bi ' + iconClass + '"></i>';

        btnOk.className = 'rv-btn rv-btn--sm';
        if (options.type === 'danger') {
            btnOk.classList.add('rv-btn--danger');
        } else if (options.type === 'success') {
            btnOk.classList.add('rv-btn--success');
        } else {
            btnOk.classList.add('rv-btn--primary');
        }

        titleH.textContent = options.title || LABEL_CONFIRM;
        messageDiv.innerHTML = options.message || '';
        btnCancel.textContent = options.cancelText || LABEL_CANCEL;
        btnOk.textContent = options.confirmText || LABEL_AGREE;

        modalOverlay.classList.add('show');

        var newBtnOk = btnOk.cloneNode(true);
        var newBtnCancel = btnCancel.cloneNode(true);
        btnOk.parentNode.replaceChild(newBtnOk, btnOk);
        btnCancel.parentNode.replaceChild(newBtnCancel, btnCancel);

        var closeModal = function () {
            modalOverlay.classList.remove('show');
        };

        newBtnCancel.addEventListener('click', function () {
            closeModal();
            if (typeof options.onCancel === 'function')
                options.onCancel();
        });

        newBtnOk.addEventListener('click', function () {
            closeModal();
            if (typeof options.onConfirm === 'function')
                options.onConfirm();
        });

        modalOverlay.addEventListener('click', function (e) {
            if (e.target === modalOverlay)
                closeModal();
        });
    };
})();
