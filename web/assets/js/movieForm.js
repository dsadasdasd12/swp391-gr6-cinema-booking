/*
 * Rap Viet CMS — Movie Form Helper (Unicode escapes for Vietnamese UI text)
 */
var RV_MOVIE_VN = {
    UNSAVED_LEAVE: 'B\u1ea1n c\u00f3 c\u00e1c thay \u0111\u1ed5i ch\u01b0a l\u01b0u. B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n r\u1eddi \u0111i?',
    CANCEL_EDIT_TITLE: 'H\u1ee7y ch\u1ec9nh s\u1eeda?',
    CANCEL_EDIT_MSG: 'B\u1ea1n \u0111\u00e3 thay \u0111\u1ed5i d\u1eef li\u1ec7u c\u1ee7a phim n\u00e0y. R\u1eddi \u0111i s\u1ebd m\u1ea5t to\u00e0n b\u1ed9 thay \u0111\u1ed5i ch\u01b0a l\u01b0u. B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn?',
    LEAVE: 'R\u1eddi \u0111i',
    GO_BACK: 'Quay l\u1ea1i',
    FORMAT_ERROR_TITLE: 'L\u1ed7i \u0111\u1ecbnh d\u1ea1ng',
    FORMAT_ERROR_MSG: 'Ch\u1ec9 ch\u1ea5p nh\u1eadn file h\u00ecnh \u1ea3nh (.jpg, .png, .webp)',
    FILE_TOO_LARGE_TITLE: 'File qu\u00e1 l\u1edbn',
    FILE_TOO_LARGE_MSG: 'K\u00edch th\u01b0\u1edbc h\u00ecnh \u1ea3nh kh\u00f4ng \u0111\u01b0\u1ee3c v\u01b0\u1ee3t qu\u00e1 5MB',
    GENRE_LIMIT_TITLE: 'Gi\u1edbi h\u1ea1n th\u1ec3 lo\u1ea1i',
    GENRE_LIMIT_MSG: 'Ch\u1ec9 \u0111\u01b0\u1ee3c ch\u1ecdn t\u1ed1i \u0111a 5 th\u1ec3 lo\u1ea1i phim'
};

document.addEventListener('DOMContentLoaded', () => {
    const movieForm = document.getElementById('rv-movie-form');
    if (!movieForm)
        return;

    let isFormDirty = false;

    const markDirty = () => {
        isFormDirty = true;
    };
    movieForm.querySelectorAll('input, select, textarea').forEach(elem => {
        elem.addEventListener('input', markDirty);
        elem.addEventListener('change', markDirty);
    });

    window.addEventListener('beforeunload', (e) => {
        if (isFormDirty) {
            e.preventDefault();
            e.returnValue = RV_MOVIE_VN.UNSAVED_LEAVE;
        }
    });

    const cancelBtn = document.getElementById('rv-form-cancel');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', (e) => {
            e.preventDefault();
            const targetUrl = cancelBtn.getAttribute('href') || '/admin/movies';

            if (isFormDirty) {
                showConfirmModal({
                    title: RV_MOVIE_VN.CANCEL_EDIT_TITLE,
                    message: RV_MOVIE_VN.CANCEL_EDIT_MSG,
                    type: 'warning',
                    confirmText: RV_MOVIE_VN.LEAVE,
                    cancelText: RV_MOVIE_VN.GO_BACK,
                    onConfirm: () => {
                        isFormDirty = false;
                        window.location.href = targetUrl;
                    }
                });
            } else {
                window.location.href = targetUrl;
            }
        });
    }

    movieForm.addEventListener('submit', () => {
        isFormDirty = false;
    });

    const descTextarea = document.getElementById('rv-movie-desc');
    const descCounter = document.getElementById('rv-movie-desc-counter');
    if (descTextarea && descCounter) {
        const updateCounter = () => {
            const length = descTextarea.value.length;
            descCounter.textContent = `${length} / 2000`;
            if (length > 1800) {
                descCounter.style.color = 'var(--danger)';
            } else {
                descCounter.style.color = 'var(--n-400)';
            }
        };
        descTextarea.addEventListener('input', updateCounter);
        updateCounter();
    }

    const posterInput = document.getElementById('rv-poster-input');
    const previewImg = document.getElementById('rv-preview-img');
    const previewPlaceholder = document.getElementById('rv-preview-placeholder');

    if (posterInput && previewImg && previewPlaceholder) {
        posterInput.addEventListener('input', (e) => {
            const url = e.target.value.trim();
            if (url) {
                previewImg.src = url;
                previewImg.style.display = 'block';
                previewPlaceholder.style.display = 'none';
            } else {
                previewImg.src = '';
                previewImg.style.display = 'none';
                previewPlaceholder.style.display = 'block';
            }
            markDirty();
        });

        // Fallback if image fails to load
        previewImg.addEventListener('error', () => {
            if (posterInput.value.trim()) {
                previewImg.style.display = 'none';
                previewPlaceholder.style.display = 'block';
            }
        });
    }

    const tagContainer = document.getElementById('rv-genre-tags-container');
    const tagSelect = document.getElementById('rv-genre-select');
    const hiddenTagsInput = document.getElementById('rv-movie-genres-hidden');

    if (tagContainer && tagSelect && hiddenTagsInput) {
        let selectedGenres = [];

        if (hiddenTagsInput.value) {
            selectedGenres = hiddenTagsInput.value.split(',').map(s => s.trim()).filter(Boolean);
        }

        const renderTags = () => {
            const existingTags = tagContainer.querySelectorAll('.rv-tag-badge');
            existingTags.forEach(tag => tag.remove());

            selectedGenres.forEach(genre => {
                const tag = document.createElement('span');
                tag.className = 'rv-badge rv-badge--nowshowing rv-tag-badge';
                tag.innerHTML = `
          ${genre}
          <i class="bi bi-x-circle-fill rv-tag-remove" data-genre="${genre}"></i>
        `;
                tagContainer.insertBefore(tag, tagSelect);
            });

            hiddenTagsInput.value = selectedGenres.join(',');

            Array.from(tagSelect.options).forEach(opt => {
                opt.disabled = selectedGenres.includes(opt.value);
            });

            tagSelect.value = '';
        };

        tagSelect.addEventListener('change', () => {
            const val = tagSelect.value;
            if (!val)
                return;

            if (selectedGenres.length >= 5) {
                showToast(RV_MOVIE_VN.GENRE_LIMIT_TITLE, RV_MOVIE_VN.GENRE_LIMIT_MSG, 'warning');
                tagSelect.value = '';
                return;
            }

            if (!selectedGenres.includes(val)) {
                selectedGenres.push(val);
                renderTags();
                markDirty();
            }
        });

        tagContainer.addEventListener('click', (e) => {
            const removeBtn = e.target.closest('.rv-tag-remove');
            if (removeBtn) {
                const genre = removeBtn.getAttribute('data-genre');
                selectedGenres = selectedGenres.filter(g => g !== genre);
                renderTags();
                markDirty();
            }
        });

        renderTags();
    }
});
