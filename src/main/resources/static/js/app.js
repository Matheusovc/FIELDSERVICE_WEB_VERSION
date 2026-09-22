/**
 * FieldService 2.0 - Client Side Scripts
 * Gerenciamento de Tema, Interações e Persistência
 */

(function () {
    'use strict';

    // 1. Inicializar tema salvo no localStorage ou cookie
    const savedTheme = localStorage.getItem('fs_theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);

    // Função global para trocar o tema
    window.setFieldServiceTheme = function (themeName) {
        if (['dark', 'light', 'super-dark'].includes(themeName)) {
            document.documentElement.setAttribute('data-theme', themeName);
            localStorage.setItem('fs_theme', themeName);
            document.cookie = `fs_theme=${themeName};path=/;max-age=31536000;SameSite=Lax`;
        }
    };

    // 2. Gerenciamento de preferências de notificação (RN011)
    window.getNotificationPreferences = function () {
        return {
            newTicket: localStorage.getItem('fs_notif_new') !== 'false',
            longRunning: localStorage.getItem('fs_notif_long') !== 'false'
        };
    };

    window.saveNotificationPreferences = function (newTicket, longRunning) {
        localStorage.setItem('fs_notif_new', newTicket);
        localStorage.setItem('fs_notif_long', longRunning);
    };
})();
