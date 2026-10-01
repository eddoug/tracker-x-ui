package com.trackerx.ui.media

import android.service.notification.NotificationListenerService

/**
 * Serviço vazio. O Android só libera MediaSessionManager.getActiveSessions() para
 * apps com "acesso a notificações"; este serviço existe para o usuário poder conceder.
 * O app não lê o conteúdo das notificações.
 */
class MediaListenerService : NotificationListenerService()
