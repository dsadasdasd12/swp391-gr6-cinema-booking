<%--
    RapViet Admin — Chi tiết phim (movie-detail.jsp)
    Servlet: AdminMovieController ?action=detail&id=X
    (Long — )
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx"       value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Chi tiết phim — ${movie.title} — RapViet Admin" scope="request" />

<%@ include file="/pages/shared/header-admin.jsp" %>
<%@ include file="/pages/shared/sidebar-admin.jsp" %>

<!-- Breadcrumb -->
<nav aria-label="breadcrumb" style="margin-bottom:1.5rem;">
    <ol class="breadcrumb" style="background:transparent;padding:0;margin:0;font-size:.85rem;">
        <li class="breadcrumb-item">
            <a href="${ctx}/admin/moviesmanagement?action=list" style="color:var(--clr-primary);text-decoration:none;">Quản lý phim</a>
        </li>
        <li class="breadcrumb-item active" style="color:var(--clr-muted);">
            <c:out value="${movie.title}"/>
        </li>
    </ol>
</nav>

<!-- Flash -->
<c:if test="${not empty sessionScope.flashSuccess}">
    <div class="flash-success"><i class="bi bi-check-circle-fill"></i> <c:out value="${sessionScope.flashSuccess}"/></div>
    <c:remove var="flashSuccess" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.flashError}">
    <div class="flash-error"><i class="bi bi-exclamation-triangle-fill"></i> <c:out value="${sessionScope.flashError}"/></div>
    <c:remove var="flashError" scope="session"/>
</c:if>

<div class="row g-4">
    <!-- ── Left: Poster + Actions ── -->
    <div class="col-lg-3">
        <div class="admin-card text-center">
            <!-- Poster -->
            <c:choose>
                <c:when test="${not empty movie.posterUrl}">
                    <img src="${movie.posterUrl}" alt="Poster ${movie.title}"
                         style="width:100%;border-radius:10px;object-fit:cover;aspect-ratio:2/3;margin-bottom:1rem;">
                </c:when>
                <c:otherwise>
                    <div style="width:100%;aspect-ratio:2/3;border-radius:10px;background:rgba(255,255,255,.04);
                                display:flex;align-items:center;justify-content:center;
                                border:2px dashed var(--clr-border);margin-bottom:1rem;">
                        <div style="color:var(--clr-muted);"><i class="bi bi-image" style="font-size:3rem;opacity:.3;"></i>
                            <p style="font-size:.75rem;">Chưa có poster</p>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

            <!-- Status badge -->
            <span class="badge-status ${movie.statusBadgeClass}"
                  style="display:inline-flex;margin-bottom:1rem;font-size:.85rem;">
                <c:out value="${movie.statusLabel}"/>
            </span>

            <!-- Action buttons -->
            <div class="d-flex flex-column gap-2">
                <a href="${ctx}/admin/moviesmanagement?action=edit&id=${movie.id}" class="btn-admin-primary">
                    <i class="bi bi-pencil"></i> Chỉnh sửa
                </a>
                <a href="${ctx}/admin/moviesmanagement?action=list" class="btn-admin-ghost">
                    <i class="bi bi-arrow-left"></i> Danh sách
                </a>
            </div>
        </div>


    </div>

    <!-- ── Right: Info + Trailer ── -->
    <div class="col-lg-9">
        <div class="admin-card mb-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <h1 style="font-size:1.4rem;font-weight:700;margin:0;">
                    <c:out value="${movie.title}"/>
                </h1>
                <small style="color:var(--clr-muted);">ID: #${movie.id}</small>
            </div>

            <!-- Info grid -->
            <div class="row g-3" style="margin-bottom:1.25rem;">
                <div class="col-md-4">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Thời lượng</div>
                    <div style="font-weight:500;"><c:out value="${movie.durationLabel}"/></div>
                </div>
                <div class="col-md-4">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Ngày khởi chiếu</div>
                    <div style="font-weight:500;"><c:out value="${movie.releaseDateLabel}"/></div>
                </div>
                <div class="col-md-4">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Đánh giá TB</div>
                    <div style="font-weight:500;">
                        ⭐ <c:out value="${movie.ratingRounded}"/> / 5
                        <small style="color:var(--clr-muted);">(${movie.reviewCount} đánh giá)</small>
                    </div>
                </div>
                <div class="col-md-6">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Đạo diễn</div>
                    <div><c:out value="${not empty movie.director ? movie.director : '—'}"/></div>
                </div>
                <div class="col-md-6">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Diễn viên</div>
                    <div><c:out value="${not empty movie.actor ? movie.actor : '—'}"/></div>
                </div>
                <div class="col-md-6">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Thể loại</div>
                    <div><c:out value="${not empty movie.categoryNames ? movie.categoryNames : '—'}"/></div>
                </div>
                <div class="col-md-6">
                    <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.25rem;">Ngôn ngữ</div>
                    <div><c:out value="${not empty movie.languageNames ? movie.languageNames : '—'}"/></div>
                </div>
            </div>

            <!-- Mô tả -->
            <div>
                <div style="color:var(--clr-muted);font-size:.78rem;font-weight:600;text-transform:uppercase;letter-spacing:.05em;margin-bottom:.5rem;">Mô tả</div>
                <p style="line-height:1.7;color:var(--clr-muted);">
                    <c:out value="${not empty movie.description ? movie.description : 'Chưa có mô tả.'}"/>
                </p>
            </div>
        </div>

        <!-- Trailer embed -->
        <c:if test="${not empty movie.embedUrl}">
            <div class="admin-card">
                <h3 style="font-size:.95rem;font-weight:600;margin-bottom:1rem;">
                    <i class="bi bi-play-circle" style="color:var(--clr-primary);margin-right:.4rem;"></i>Trailer
                </h3>
                <div style="position:relative;padding-bottom:56.25%;border-radius:8px;overflow:hidden;">
                    <iframe src="${movie.embedUrl}" frameborder="0" allowfullscreen
                            style="position:absolute;top:0;left:0;width:100%;height:100%;"></iframe>
                </div>
            </div>
        </c:if>
    </div>
</div>

</main>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
        crossorigin="anonymous"></script>
</body>
</html>
