/*
 * Rap Viet CMS — Charts Helper (ASCII source; Vietnamese via Unicode escapes)
 */
var RV_VN = {
    REVENUE_VND: 'Doanh thu (VND)',
    TICKETS_SOLD_COUNT: 'S\u1ed1 v\u00e9 b\u00e1n ra',
    OCCUPANCY_TOOLTIP: 'T\u1ef7 l\u1ec7 l\u1ea5p \u0111\u1ea7y: ',
    TICKETS_SOLD_LEGEND: 'V\u00e9 B\u00e1n Ra',
    SHOWTIME_LEGEND: 'Su\u1ea5t Chi\u1ebfu',
    TICKETS_AXIS: 'S\u1ed1 v\u00e9 b\u00e1n',
    SHOWTIME_AXIS: 'S\u1ed1 su\u1ea5t chi\u1ebfu',
    DAYS: [
        'Th\u1ee9 2',
        'Th\u1ee9 3',
        'Th\u1ee9 4',
        'Th\u1ee9 5',
        'Th\u1ee9 6',
        'Th\u1ee9 7',
        'Ch\u1ee7 Nh\u1eadt'
    ],
    HEATMAP_AT: ' l\u00fac ',
    HEATMAP_OCCUPANCY_SUFFIX: '% l\u1ea5p \u0111\u1ea7y'
};

window.RVCharts = {
    colors: {
        primary: '#1A3C5E',
        primaryLight: '#2563A8',
        primaryExtraLight: 'rgba(37, 99, 168, 0.1)',
        accent: '#E8B84B',
        accentLight: 'rgba(232, 184, 75, 0.1)',
        success: '#16A34A',
        danger: '#DC2626',
        border: '#E2E8F0',
        text: '#475569',
        grid: '#F1F5F9'
    },

    createLineChart(canvasId, labels, dataPoints, datasetLabel) {
        if (datasetLabel === undefined)
            datasetLabel = RV_VN.REVENUE_VND;
        const ctx = document.getElementById(canvasId);
        if (!ctx)
            return null;

        return new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                        label: datasetLabel,
                        data: dataPoints,
                        borderColor: this.colors.primaryLight,
                        backgroundColor: this.colors.primaryExtraLight,
                        borderWidth: 2,
                        fill: true,
                        tension: 0.3,
                        pointBackgroundColor: this.colors.primaryLight,
                        pointHoverRadius: 6
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {display: false},
                    tooltip: {
                        padding: 10,
                        backgroundColor: '#0F172A',
                        titleFont: {size: 13, weight: 'semibold', family: 'Inter'},
                        bodyFont: {size: 12, family: 'Inter'},
                        callbacks: {
                            label: function (context) {
                                const val = context.raw;
                                if (typeof val === 'number') {
                                    return ' ' + val.toLocaleString('vi-VN') + ' \u20ab';
                                }
                                return ' ' + val;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {display: false},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}}
                    },
                    y: {
                        grid: {color: this.colors.grid},
                        ticks: {
                            color: this.colors.text,
                            font: {family: 'Inter', size: 11},
                            callback: function (value) {
                                if (value >= 1e6)
                                    return (value / 1e6) + 'M';
                                if (value >= 1e3)
                                    return (value / 1e3) + 'K';
                                return value;
                            }
                        }
                    }
                }
            }
        });
    },

    createBarChart(canvasId, labels, dataPoints, datasetLabel) {
        if (datasetLabel === undefined)
            datasetLabel = RV_VN.TICKETS_SOLD_COUNT;
        const ctx = document.getElementById(canvasId);
        if (!ctx)
            return null;

        return new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                        label: datasetLabel,
                        data: dataPoints,
                        backgroundColor: this.colors.accent,
                        hoverBackgroundColor: '#D1A337',
                        borderRadius: 4,
                        maxBarThickness: 32
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {display: false},
                    tooltip: {
                        padding: 10,
                        backgroundColor: '#0F172A',
                        titleFont: {size: 13, weight: 'semibold', family: 'Inter'},
                        bodyFont: {size: 12, family: 'Inter'}
                    }
                },
                scales: {
                    x: {
                        grid: {display: false},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}}
                    },
                    y: {
                        grid: {color: this.colors.grid},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}}
                    }
                }
            }
        });
    },

    createHorizontalBarChart(canvasId, labels, dataPoints) {
        const ctx = document.getElementById(canvasId);
        if (!ctx)
            return null;

        return new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                        data: dataPoints,
                        backgroundColor: this.colors.primary,
                        hoverBackgroundColor: this.colors.primaryLight,
                        borderRadius: 4,
                        maxBarThickness: 20
                    }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {display: false},
                    tooltip: {
                        padding: 10,
                        backgroundColor: '#0F172A',
                        bodyFont: {family: 'Inter', size: 12},
                        callbacks: {
                            label: (context) => RV_VN.OCCUPANCY_TOOLTIP + context.raw + '%'
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {color: this.colors.grid},
                        ticks: {
                            color: this.colors.text,
                            font: {family: 'Inter', size: 11},
                            callback: (value) => value + '%'
                        },
                        max: 100
                    },
                    y: {
                        grid: {display: false},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}}
                    }
                }
            }
        });
    },

    createComboChart(canvasId, labels, ticketData, showtimeData) {
        const ctx = document.getElementById(canvasId);
        if (!ctx)
            return null;

        return new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        type: 'bar',
                        label: RV_VN.TICKETS_SOLD_LEGEND,
                        data: ticketData,
                        backgroundColor: 'rgba(37, 99, 168, 0.75)',
                        borderRadius: 4,
                        yAxisID: 'yTickets'
                    },
                    {
                        type: 'line',
                        label: RV_VN.SHOWTIME_LEGEND,
                        data: showtimeData,
                        borderColor: this.colors.accent,
                        borderWidth: 2,
                        tension: 0.1,
                        fill: false,
                        pointBackgroundColor: this.colors.accent,
                        yAxisID: 'yShowtimes'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {font: {family: 'Inter', size: 12}, color: this.colors.text}
                    },
                    tooltip: {padding: 10, backgroundColor: '#0F172A', bodyFont: {family: 'Inter'}}
                },
                scales: {
                    x: {
                        grid: {display: false},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}}
                    },
                    yTickets: {
                        type: 'linear',
                        position: 'left',
                        grid: {color: this.colors.grid},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}},
                        title: {display: true, text: RV_VN.TICKETS_AXIS, font: {family: 'Inter', size: 11}}
                    },
                    yShowtimes: {
                        type: 'linear',
                        position: 'right',
                        grid: {display: false},
                        ticks: {color: this.colors.text, font: {family: 'Inter', size: 11}},
                        title: {display: true, text: RV_VN.SHOWTIME_AXIS, font: {family: 'Inter', size: 11}}
                    }
                }
            }
        });
    },

    renderPeakHoursHeatmap(containerId, dataMatrix) {
        const container = document.getElementById(containerId);
        if (!container)
            return;

        container.innerHTML = '';
        container.className = 'rv-heatmap-grid';

        const days = RV_VN.DAYS;
        const hours = ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00', '00:00'];

        if (!document.getElementById('rv-heatmap-styles')) {
            const styles = document.createElement('style');
            styles.id = 'rv-heatmap-styles';
            styles.textContent = `
        .rv-heatmap-grid {
          display: grid;
          grid-template-columns: 80px repeat(9, 1fr);
          gap: 4px;
          font-family: 'Inter', sans-serif;
          margin-top: 15px;
        }
        .rv-heatmap-cell {
          height: 36px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 11px;
          font-weight: 500;
          color: rgba(15, 23, 42, 0.7);
          transition: transform 0.1s ease;
          cursor: pointer;
        }
        .rv-heatmap-cell:hover {
          transform: scale(1.05);
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
          color: #000;
        }
        .rv-heatmap-header {
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 11px;
          font-weight: 600;
          color: var(--n-500);
          height: 30px;
        }
        .rv-heatmap-day-label {
          display: flex;
          align-items: center;
          justify-content: flex-start;
          font-size: 12px;
          font-weight: 600;
          color: var(--n-700);
          font-family: 'Inter', sans-serif;
        }
      `;
            document.head.appendChild(styles);
        }

        container.appendChild(document.createElement('div'));

        hours.forEach(function (hour) {
            const header = document.createElement('div');
            header.className = 'rv-heatmap-header';
            header.textContent = hour;
            container.appendChild(header);
        });

        for (let d = 0; d < 7; d++) {
            const dayLabel = document.createElement('div');
            dayLabel.className = 'rv-heatmap-day-label';
            dayLabel.textContent = days[d];
            container.appendChild(dayLabel);

            for (let h = 0; h < 9; h++) {
                const val = dataMatrix[d] ? (dataMatrix[d][h] || 0) : 0;
                const cell = document.createElement('div');
                cell.className = 'rv-heatmap-cell';
                cell.setAttribute(
                        'data-tooltip',
                        days[d] + RV_VN.HEATMAP_AT + hours[h] + ': ' + val + RV_VN.HEATMAP_OCCUPANCY_SUFFIX
                        );

                let bgColor;
                let textColor;
                if (val < 10) {
                    bgColor = '#F8FAFC';
                    textColor = '#94A3B8';
                } else if (val < 30) {
                    bgColor = '#DBEAFE';
                    textColor = '#1D4ED8';
                } else if (val < 60) {
                    bgColor = '#93C5FD';
                    textColor = '#1E40AF';
                } else if (val < 80) {
                    bgColor = '#3B82F6';
                    textColor = '#FFFFFF';
                } else {
                    bgColor = '#1D4ED8';
                    textColor = '#FFFFFF';
                }

                cell.style.backgroundColor = bgColor;
                cell.style.color = textColor;
                cell.textContent = val + '%';
                container.appendChild(cell);
            }
        }
    }
};
