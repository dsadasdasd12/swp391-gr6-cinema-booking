<style>
    :root {
        --glass-bg: hsla(222, 47%, 12%, 0.7);
        --border-color: hsla(217, 30%, 20%, 0.5);
        --primary: hsl(224, 89%, 60%);
        --emerald: hsl(150, 84%, 37%);
        --muted-text: hsl(215, 20%, 65%);
    }

    /* Grid of Showtimes */
    .showtimes-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
        gap: 25px;
    }

    .showtime-card {
        background: var(--glass-bg);
        border: 1px solid var(--border-color);
        border-radius: 12px;
        padding: 24px;
        backdrop-filter: blur(16px);
        transition: transform 0.3s, border-color 0.3s;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        position: relative;
        overflow: hidden;
    }

    .showtime-card::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 4px;
        background: linear-gradient(90deg, #ff3366, var(--primary));
    }

    .showtime-card:hover {
        transform: translateY(-5px);
        border-color: rgba(99, 102, 241, 0.5);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
    }

    .movie-title {
        font-size: 18px;
        font-weight: 700;
        margin: 0 0 10px 0;
        color: #fff;
    }

    .showtime-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }

    .time-badge {
        background: rgba(255, 255, 255, 0.08);
        padding: 6px 12px;
        border-radius: 6px;
        font-size: 13px;
        font-weight: 600;
        color: #a5b4fc;
    }

    .hall-badge {
        background: var(--primary);
        color: white;
        padding: 4px 10px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: bold;
    }

    /* Occupancy Ring Chart SVG */
    .occupancy-box {
        display: flex;
        align-items: center;
        gap: 15px;
        background: rgba(255, 255, 255, 0.03);
        padding: 12px;
        border-radius: 8px;
        border: 1px solid rgba(255, 255, 255, 0.05);
        margin-bottom: 20px;
    }

    .ring-container {
        position: relative;
        width: 60px;
        height: 60px;
    }

    .ring-svg {
        transform: rotate(-90deg);
    }

    .ring-bg {
        fill: none;
        stroke: rgba(255, 255, 255, 0.08);
        stroke-width: 6;
    }

    .ring-fill {
        fill: none;
        stroke: var(--emerald);
        stroke-width: 6;
        stroke-linecap: round;
        transition: stroke-dashoffset 0.6s ease;
    }

    .ring-text {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 12px;
        font-weight: 700;
        color: #34d399;
    }

    .pricing-section {
        border-top: 1px solid var(--border-color);
        padding-top: 15px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .btn-pricing {
        background: rgba(99, 102, 241, 0.15);
        border: 1px solid var(--primary);
        color: #a5b4fc;
        padding: 8px 16px;
        border-radius: 6px;
        font-size: 13px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s;
    }

    .btn-pricing:hover {
        background: var(--primary);
        color: #fff;
        box-shadow: 0 0 10px rgba(99, 102, 241, 0.4);
    }

    /* Modal Popup styles */
    .modal {
        display: none;
        position: fixed;
        z-index: 100;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(3, 7, 18, 0.85);
        backdrop-filter: blur(12px);
        align-items: center;
        justify-content: center;
    }

    .modal-content {
        background: hsla(222, 47%, 10%, 0.95);
        border: 1px solid var(--border-color);
        border-radius: 16px;
        width: 90%;
        max-width: 460px;
        padding: 30px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }

    .modal-header h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
    }

    .close-btn {
        background: none;
        border: none;
        color: var(--muted-text);
        font-size: 24px;
        cursor: pointer;
        transition: color 0.3s;
    }

    .close-btn:hover {
        color: #fff;
    }

    .form-group {
        margin-bottom: 20px;
    }

    .form-group label {
        display: block;
        font-size: 13px;
        font-weight: 600;
        color: var(--muted-text);
        margin-bottom: 8px;
    }

    .form-group input, .form-group select {
        width: 100%;
        padding: 12px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid var(--border-color);
        border-radius: 8px;
        color: #fff;
        font-size: 14px;
        box-sizing: border-box;
        outline: none;
        transition: border-color 0.3s;
    }

    .form-group select option {
        background-color: #111827;
        color: #fff;
    }

    .form-group input:focus, .form-group select:focus {
        border-color: var(--primary);
    }

    .btn-submit {
        width: 100%;
        background: linear-gradient(135deg, #ff3366 0%, #e11d48 100%);
        border: none;
        color: white;
        padding: 14px;
        border-radius: 8px;
        font-size: 15px;
        font-weight: bold;
        cursor: pointer;
        box-shadow: 0 4px 15px rgba(255, 51, 102, 0.4);
        transition: opacity 0.3s;
    }

    .btn-submit:hover {
        opacity: 0.9;
    }
</style>
