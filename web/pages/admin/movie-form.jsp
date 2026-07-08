<%--
    Rạp Việt CMS — Premium Add / Edit Movie Form
    URL: /admin/moviesmanagement?action=new (add) or ?action=edit&id={id} (update)
    Servlet: AdminMovieController
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${formAction == 'update'}" />
<c:set var="pageTitle" value="${isEdit ? 'Sửa phim' : 'Thêm phim mới'} — Rạp Việt CMS" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- Link local JS helper for form functionality -->
<script src="${ctx}/assets/js/movieForm.js" charset="UTF-8" defer></script>

<!-- ── PAGE HEADER ── -->
<div class="rv-page-header">
    <div class="rv-page-header__left">
        <!-- Breadcrumb -->
        <div class="rv-breadcrumb">
            <a href="${ctx}/admin/moviesmanagement?action=list">Quản lý phim</a>
            <i class="bi bi-chevron-right rv-breadcrumb__sep"></i>
            <span class="rv-breadcrumb__current">
                <c:choose>
                    <c:when test="${isEdit}">Sửa phim: <c:out value="${movie.title}"/></c:when>
                    <c:otherwise>Thêm phim mới</c:otherwise>
                </c:choose>
            </span>
        </div>
        <h1 class="rv-page-title">
            <c:choose>
                <c:when test="${isEdit}">Chỉnh sửa thông tin phim</c:when>
                <c:otherwise>Thêm phim mới</c:otherwise>
            </c:choose>
        </h1>
        <p class="rv-page-subtitle">Nhập thông tin phim, poster, trailer và danh mục chiếu tương ứng.</p>
    </div>
</div>

<!-- Validation errors alert box -->
<c:if test="${not empty errors}">
    <div class="rv-banner rv-banner--warning" style="margin-bottom: 24px;">
        <i class="bi bi-exclamation-triangle-fill" style="font-size: 20px;"></i>
        <div>
            <div style="font-weight: 600; margin-bottom: 4px;">Vui lòng kiểm tra các lỗi sau:</div>
            <ul style="margin: 0; padding-left: 20px; font-size: 13px;">
                <c:forEach var="err" items="${errors}">
                    <li><c:out value="${err}"/></li>
                </c:forEach>
            </ul>
        </div>
    </div>
</c:if>

<form method="post" action="${ctx}/admin/moviesmanagement" id="rv-movie-form" novalidate enctype="multipart/form-data">
    <input type="hidden" name="action" value="${formAction}">
    <c:if test="${isEdit}">
        <input type="hidden" name="id" value="${movie.id}">
        <!-- Save existing URLs if not updated -->
        <input type="hidden" name="existingPosterUrl" value="${movie.posterUrl}">
    </c:if>

    <div class="rv-form-grid" style="display: grid; grid-template-columns: 2fr 1fr; gap: var(--s-6);">
        <!-- ── LEFT PANEL: MOVIE INFORMATION ── -->
        <div class="rv-card">
            <div class="rv-card__header">
                <span class="rv-card__title">Thông tin phim chi tiết</span>
            </div>
            <div class="rv-card__body" style="display: flex; flex-direction: column; gap: var(--s-4);">
                
                <!-- Movie Title -->
                <div class="rv-form-group">
                    <label class="rv-label" for="title">Tên phim *</label>
                    <input type="text" id="title" name="title" class="rv-input" maxlength="200" required placeholder="Nhập tên phim tiếng Việt / tiếng Anh..." value="<c:out value='${movie.title}'/>">
                    <div class="rv-feedback rv-feedback--invalid" id="err-title"></div>
                </div>

                <!-- Genre Multi-select tag input -->
                <div class="rv-form-group">
                    <label class="rv-label">Thể loại * <span style="font-weight: normal; font-size: 12px; color: var(--n-400);">(Chọn tối đa 5 thể loại)</span></label>
                    
                    <div id="rv-genre-tags-container" class="rv-tag-container" style="display: flex; flex-wrap: wrap; gap: var(--s-2); padding: var(--s-2); border: 1px solid var(--border); border-radius: var(--r-md); min-height: 42px; align-items: center; background: var(--n-50);">
                        <!-- Select dropdown styled inside the tag container -->
                        <select id="rv-genre-select" style="border: none; background: transparent; outline: none; color: var(--n-600); font-family: inherit; font-size: var(--text-base); cursor: pointer; padding: 2px 4px; min-width: 140px;">
                            <option value="">+ Chọn thể loại</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.name}"><c:out value="${cat.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <!-- Hidden input to serialize categories -->
                    <input type="hidden" name="genres" id="rv-movie-genres-hidden" value="<c:out value='${movie.categoryNames}'/>">
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-4);">
                    <!-- End Date -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="endDate">NgÃ y káº¿t thÃºc chiáº¿u *</label>
                        <input type="date" id="endDate" name="endDate" class="rv-input" required value="${movie.endDateForInput}">
                    </div>

                    <!-- Auto Status -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="status">Tráº¡ng thÃ¡i phÃ¡t hÃ nh</label>
                        <select id="status" class="rv-select" disabled>
                            <option value="COMING_SOON" ${movie.status == 'COMING_SOON' ? 'selected' : ''}>Sáº¯p chiáº¿u (Coming Soon)</option>
                            <option value="NOW_SHOWING" ${movie.status == 'NOW_SHOWING' ? 'selected' : ''}>Äang chiáº¿u (Now Showing)</option>
                            <option value="ENDED" ${movie.status == 'ENDED' ? 'selected' : ''}>ÄÃ£ káº¿t thÃºc (Ended)</option>
                        </select>
                        <span style="font-size: 11px; color: var(--n-400); margin-top: 4px; display: block;">
                            Tá»± Ä‘á»™ng tÃ­nh theo ngÃ y khá»Ÿi chiáº¿u vÃ  ngÃ y káº¿t thÃºc.
                        </span>
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-4);">
                    <!-- Duration -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="durationMin">Thời lượng (phút) *</label>
                        <input type="number" id="durationMin" name="durationMin" class="rv-input" min="1" max="999" required placeholder="Ví dụ: 120" value="${movie.durationMin > 0 ? movie.durationMin : ''}">
                        <div class="rv-feedback rv-feedback--invalid" id="err-duration"></div>
                    </div>

                    <!-- Release Date -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="releaseDate">Ngày khởi chiếu *</label>
                        <input type="date" id="releaseDate" name="releaseDate" class="rv-input" required value="${movie.releaseDateForInput}">
                    </div>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-4);">
                    <!-- Language Selection -->
                    <div class="rv-form-group">
                        <label class="rv-label" for="language">Ngôn ngữ chính *</label>
                        <select id="language" name="language" class="rv-select" required>
                            <option value="">-- Chọn ngôn ngữ --</option>
                            <option value="Vietnamese" ${movie.primaryLanguageFormValue == 'Vietnamese' ? 'selected' : ''}>Tiếng Việt (Phụ đề / Lồng tiếng)</option>
                            <option value="English" ${movie.primaryLanguageFormValue == 'English' ? 'selected' : ''}>Tiếng Anh (Phụ đề)</option>
                            <option value="Korean" ${movie.primaryLanguageFormValue == 'Korean' ? 'selected' : ''}>Tiếng Hàn (Phụ đề)</option>
                            <option value="Japanese" ${movie.primaryLanguageFormValue == 'Japanese' ? 'selected' : ''}>Tiếng Nhật (Phụ đề)</option>
                            <option value="Mixed" ${movie.primaryLanguageFormValue == 'Mixed' ? 'selected' : ''}>Hỗn hợp (Lồng tiếng + Phụ đề)</option>
                        </select>
                    </div>

                    <!-- Status Selection -->
                    <div class="rv-form-group" style="display:none;">
                        <label class="rv-label" for="status">Trạng thái phát hành *</label>
                        <select id="statusLegacy" class="rv-select" disabled>
                            <option value="">-- Chọn trạng thái --</option>
                            <option value="COMING_SOON" ${movie.status == 'COMING_SOON' ? 'selected' : ''}>Sắp chiếu (Coming Soon)</option>
                            <option value="NOW_SHOWING" ${movie.status == 'NOW_SHOWING' ? 'selected' : ''}>Đang chiếu (Now Showing)</option>
                            <option value="ENDED" ${movie.status == 'ENDED' ? 'selected' : ''}>Đã kết thúc (Ended)</option>
                        </select>
                    </div>
                </div>

                <!-- Accordion / Fieldset: Additional Information -->
                <div style="border-top: 1px solid var(--border); padding-top: var(--s-4); margin-top: var(--s-2);">
                    <div style="font-weight: 600; color: var(--n-700); margin-bottom: var(--s-3); font-size: 14px;">Thông tin bổ sung (Không bắt buộc)</div>
                    
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-4);">
                        <!-- Director -->
                        <div class="rv-form-group">
                            <label class="rv-label" for="director">Đạo diễn</label>
                            <input type="text" id="director" name="director" class="rv-input" placeholder="Tên đạo diễn..." value="<c:out value='${movie.director}'/>">
                        </div>

                        <!-- Actors -->
                        <div class="rv-form-group">
                            <label class="rv-label" for="actor">Diễn viên chính</label>
                            <input type="text" id="actor" name="actor" class="rv-input" placeholder="Ngăn cách bằng dấu phẩy..." value="<c:out value='${movie.actor}'/>">
                        </div>
                    </div>
                </div>

            </div>
        </div>

        <!-- ── RIGHT PANEL: MEDIA & DESCRIPTION ── -->
        <div style="display: flex; flex-direction: column; gap: var(--s-6);">
            <!-- Poster Image Upload Zone -->
            <div class="rv-card">
                <div class="rv-card__header">
                    <span class="rv-card__title">Poster phim *</span>
                </div>
                <div class="rv-card__body">
                    <!-- Custom Drag and drop zone -->
                    <div id="rv-upload-zone" class="rv-upload-zone" style="border: 2px dashed var(--border); border-radius: var(--r-lg); background: var(--n-50); aspect-ratio: 2/3; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--s-4); text-align: center; cursor: pointer; position: relative; transition: all var(--ease); overflow: hidden;">
                        
                        <!-- Upload placeholder -->
                        <div id="rv-upload-placeholder" style="display: ${not empty movie.posterWebPath ? 'none' : 'flex'}; flex-direction: column; align-items: center; gap: var(--s-2);">
                            <i class="bi bi-cloud-arrow-up" style="font-size: 40px; color: var(--n-400);"></i>
                            <div style="font-weight: 500; color: var(--n-700);">Kéo thả ảnh hoặc click để upload</div>
                            <div style="font-size: 11px; color: var(--n-400);">Chấp nhận JPG, PNG, WEBP tối đa 5MB</div>
                        </div>

                        <!-- Local Preview -->
                        <div id="rv-poster-preview" style="display: ${not empty movie.posterWebPath ? 'block' : 'none'}; width: 100%; height: 100%;">
                            <img id="rv-preview-img" src="${not empty movie.posterWebPath ? ctx.concat('/').concat(movie.posterWebPath) : ''}" alt="poster-preview" style="width: 100%; height: 100%; object-fit: cover; border-radius: var(--r-md);">
                            
                            <button type="button" id="rv-clear-poster" class="rv-btn rv-btn--danger rv-btn--icon" style="position: absolute; top: var(--s-3); right: var(--s-3); z-index: 10; border-radius: 50%; width: 32px; height: 32px;" title="Xóa ảnh poster">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </div>

                    <!-- Hidden Input -->
                    <input type="file" name="posterFile" id="rv-poster-input" accept="image/jpeg, image/png, image/webp" style="display: none;">
                </div>
            </div>

            <!-- Trailer: chỉ link YouTube -->
            <div class="rv-card">
                <div class="rv-card__header">
                    <span class="rv-card__title">Trailer YouTube</span>
                </div>
                <div class="rv-card__body" style="display: flex; flex-direction: column; gap: var(--s-3);">
                    <div class="rv-form-group">
                        <label class="rv-label" for="trailerUrl">Link YouTube</label>
                        <input type="url" id="trailerUrl" name="trailerUrl" class="rv-input"
                               placeholder="https://www.youtube.com/watch?v=..."
                               value="<c:out value='${movie.trailerUrl}'/>">
                        <span style="font-size: 11px; color: var(--n-400); margin-top: 4px; display: block;">
                            Chỉ dán link YouTube (watch / youtu.be / embed). Không upload file video.
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Description Details full width -->
    <div class="rv-card" style="margin-top: var(--s-6);">
        <div class="rv-card__header">
            <span class="rv-card__title">Mô tả tóm tắt phim *</span>
        </div>
        <div class="rv-card__body">
            <div class="rv-form-group">
                <textarea id="rv-movie-desc" name="description" class="rv-textarea" rows="6" maxlength="2000" required placeholder="Nhập nội dung giới thiệu, tóm tắt cốt truyện và các tình tiết chính của phim..."><c:out value="${movie.description}"/></textarea>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: var(--s-2);">
                    <div class="rv-feedback rv-feedback--invalid" id="err-desc"></div>
                    <div id="rv-movie-desc-counter" style="font-size: 12px; color: var(--n-400);">0 / 2000</div>
                </div>
            </div>
        </div>
    </div>

    <!-- ── FOOTER ACTIONS ── -->
    <div class="rv-card" style="margin-top: var(--s-6); border-top: 1px solid var(--border);">
        <div class="rv-card__body" style="display: flex; justify-content: flex-end; gap: var(--s-4); padding: var(--s-4) var(--s-6);">
            <a href="${ctx}/admin/moviesmanagement?action=list" id="rv-form-cancel" class="rv-btn rv-btn--ghost">
                Hủy bỏ
            </a>
            <button type="submit" class="rv-btn rv-btn--primary" id="rv-form-save">
                <i class="bi bi-cloud-check"></i>
                <c:choose>
                    <c:when test="${isEdit}">Lưu thay đổi</c:when>
                    <c:otherwise>Lưu phim mới</c:otherwise>
                </c:choose>
            </button>
        </div>
    </div>
</form>

</main>
</div>

<!-- Validation and dirty Form check -->
<script>
document.getElementById('rv-movie-form').addEventListener('submit', function(e) {
    let isValid = true;
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const titleVal = document.getElementById('title').value.trim();
    const titleErr = document.getElementById('err-title');
    if (!titleVal) {
        titleErr.textContent = 'Tên phim là trường bắt buộc và không thể để trống.';
        isValid = false;
    } else {
        titleErr.textContent = '';
    }

    const durationVal = parseInt(document.getElementById('durationMin').value, 10);
    const durationErr = document.getElementById('err-duration');
    if (isNaN(durationVal) || durationVal <= 0 || durationVal > 999) {
        durationErr.textContent = 'Thời lượng phim không hợp lệ (từ 1 đến 999 phút).';
        isValid = false;
    } else {
        durationErr.textContent = '';
    }

    const descVal = document.getElementById('rv-movie-desc').value.trim();
    const descErr = document.getElementById('err-desc');
    if (!descVal) {
        descErr.textContent = 'Mô tả tóm tắt phim là trường bắt buộc.';
        isValid = false;
    } else {
        descErr.textContent = '';
    }

    const genresVal = (document.getElementById('rv-movie-genres-hidden').value || '').trim();
    if (!genresVal) {
        isValid = false;
        window.showToast('Thiếu thể loại', 'Vui lòng chọn ít nhất một thể loại.', 'error');
    }

    const langVal = document.getElementById('language').value;
    if (!langVal) {
        isValid = false;
        window.showToast('Thiếu ngôn ngữ', 'Vui lòng chọn ngôn ngữ chính.', 'error');
    }

    updateAutoStatus();
    const statusVal = document.getElementById('status').value;
    if (!statusVal) {
        isValid = false;
        window.showToast('Thiếu trạng thái', 'Vui lòng chọn trạng thái phát hành.', 'error');
    }

    const releaseInput = document.getElementById('releaseDate').value;
    const endInput = document.getElementById('endDate').value;
    if (!releaseInput) {
        isValid = false;
        window.showToast('Thiếu ngày chiếu', 'Vui lòng chọn ngày khởi chiếu.', 'error');
    } else if (statusVal) {
        const release = new Date(releaseInput + 'T00:00:00');
        if (statusVal === 'COMING_SOON' && release < today) {
            isValid = false;
            window.showToast('Ngày không hợp lệ', 'Phim sắp chiếu: ngày khởi chiếu không được trước hôm nay.', 'error');
        } else if (statusVal === 'NOW_SHOWING' && release > today) {
            isValid = false;
            window.showToast('Ngày không hợp lệ', 'Phim đang chiếu: ngày khởi chiếu không được sau hôm nay.', 'error');
        } else if (statusVal === 'ENDED' && release >= today) {
            isValid = false;
            window.showToast('Ngày không hợp lệ', 'Phim đã kết thúc: ngày khởi chiếu phải trước hôm nay.', 'error');
        }
    }

    if (!endInput) {
        isValid = false;
        window.showToast('Thiáº¿u ngÃ y káº¿t thÃºc', 'Vui lÃ²ng chá»n ngÃ y káº¿t thÃºc chiáº¿u.', 'error');
    }
    if (releaseInput && endInput) {
        const release = new Date(releaseInput + 'T00:00:00');
        const end = new Date(endInput + 'T00:00:00');
        if (end < release) {
            isValid = false;
            window.showToast('NgÃ y khÃ´ng há»£p lá»‡', 'NgÃ y káº¿t thÃºc chiáº¿u khÃ´ng Ä‘Æ°á»£c trÆ°á»›c ngÃ y khá»Ÿi chiáº¿u.', 'error');
        }
    }

    const posterInput = document.getElementById('rv-poster-input');
    const hasPosterPreview = document.getElementById('rv-poster-preview').style.display !== 'none';
    const hasFile = posterInput.files && posterInput.files.length > 0;
    if (!hasFile && !hasPosterPreview) {
        isValid = false;
        window.showToast('Thiếu poster', 'Vui lòng tải lên poster phim.', 'error');
    }

    if (!isValid) {
        e.preventDefault();
        if (titleErr.textContent || durationErr.textContent || descErr.textContent) {
            window.showToast('Lỗi nhập liệu', 'Vui lòng kiểm tra lại các trường bắt buộc.', 'error');
        }
    } else {
        document.getElementById('rv-form-save').classList.add('loading');
    }
});

(function syncReleaseDateByStatus() {
    return;
    const statusEl = document.getElementById('status');
    const dateEl = document.getElementById('releaseDate');
    if (!statusEl || !dateEl) return;
    const todayIso = new Date().toISOString().slice(0, 10);
    const apply = () => {
        dateEl.removeAttribute('min');
        dateEl.removeAttribute('max');
        if (statusEl.value === 'COMING_SOON') {
            dateEl.min = todayIso;
        } else if (statusEl.value === 'NOW_SHOWING') {
            dateEl.max = todayIso;
        } else if (statusEl.value === 'ENDED') {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            dateEl.max = yesterday.toISOString().slice(0, 10);
        }
    };
    statusEl.addEventListener('change', apply);
    apply();
})();

function updateAutoStatus() {
    const statusEl = document.getElementById('status');
    const releaseEl = document.getElementById('releaseDate');
    const endEl = document.getElementById('endDate');
    if (!statusEl || !releaseEl || !endEl || !releaseEl.value || !endEl.value) {
        return;
    }

    endEl.min = releaseEl.value;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const release = new Date(releaseEl.value + 'T00:00:00');
    const end = new Date(endEl.value + 'T00:00:00');

    if (release > today) {
        statusEl.value = 'COMING_SOON';
    } else if (end < today) {
        statusEl.value = 'ENDED';
    } else {
        statusEl.value = 'NOW_SHOWING';
    }
}

(function bindAutoStatusByDate() {
    const releaseEl = document.getElementById('releaseDate');
    const endEl = document.getElementById('endDate');
    if (!releaseEl || !endEl) {
        return;
    }
    releaseEl.addEventListener('change', updateAutoStatus);
    endEl.addEventListener('change', updateAutoStatus);
    updateAutoStatus();
})();
</script>

</body>
</html>
