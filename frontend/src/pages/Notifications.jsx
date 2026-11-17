import { useEffect, useState } from 'react';
import api from '../api/axios';

export default function Notifications() {
  const [notifs, setNotifs]   = useState([]);
  const [unread, setUnread]   = useState(0);
  const [loading, setLoading] = useState(true);

  const load = () => {
    api.get('/notifications')
      .then(r => { setNotifs(r.data.notifications || []); setUnread(r.data.unreadCount || 0); })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const markAllRead = async () => {
    await api.patch('/notifications/read-all');
    load();
  };

  const markRead = async (id) => {
    await api.patch(`/notifications/${id}/read`);
    load();
  };

  const fmt = dt => dt ? new Date(dt).toLocaleString() : '';

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>📬 Notifications</h1>
          <p>{unread} unread notification{unread !== 1 ? 's' : ''}</p>
        </div>
        {unread > 0 && (
          <button className="btn btn-ghost" onClick={markAllRead}>✓ Mark all read</button>
        )}
      </div>

      {loading ? (
        <div className="loading"><div className="spinner" /><span>Loading...</span></div>
      ) : notifs.length === 0 ? (
        <div className="empty-state">
          <span className="icon">📬</span>
          <h3>No notifications</h3>
          <p>You're all caught up! Notifications appear here when reminders fire.</p>
        </div>
      ) : (
        <div className="card">
          {notifs.map(n => (
            <div key={n.id} className={`notif-item ${n.isRead ? 'read' : ''}`}
              onClick={() => !n.isRead && markRead(n.id)} style={{cursor: n.isRead ? 'default' : 'pointer'}}>
              <div className="notif-dot-indicator" />
              <div style={{flex:1}}>
                <h4>{n.title}</h4>
                <p>{n.message}</p>
                <time>{fmt(n.createdAt)}</time>
              </div>
              {!n.isRead && (
                <span className="badge badge-primary">New</span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
