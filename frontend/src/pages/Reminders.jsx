import { useEffect, useState } from 'react';
import api from '../api/axios';

const statusColor = s => ({ Pending:'badge-warning', Sent:'badge-success', Failed:'badge-danger' }[s] || 'badge-ghost');

export default function Reminders() {
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    api.get('/reminders')
      .then(r => setReminders(r.data.reminders || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const fmt = dt => dt ? new Date(dt).toLocaleString() : '—';

  return (
    <div className="page">
      <div className="page-header">
        <div><h1>🔔 Reminders</h1><p>All scheduled reminders for your tasks.</p></div>
      </div>

      {loading ? (
        <div className="loading"><div className="spinner" /><span>Loading...</span></div>
      ) : reminders.length === 0 ? (
        <div className="empty-state">
          <span className="icon">🔔</span>
          <h3>No reminders yet</h3>
          <p>Set a reminder when creating a task.</p>
        </div>
      ) : (
        <div className="card">
          <div className="tasks-grid">
            {reminders.map(r => (
              <div key={r.id} className="task-card">
                <div style={{fontSize:'1.5rem'}}>🔔</div>
                <div className="task-body">
                  <div className="task-title">Task #{r.taskId}</div>
                  <div className="task-meta">
                    <span className="badge badge-info">📅 {fmt(r.remindAt)}</span>
                    <span className="badge badge-ghost">{r.reminderType}</span>
                    <span className={`badge ${statusColor(r.status)}`}>{r.status}</span>
                    {r.sentAt && <span className="badge badge-ghost">Sent: {fmt(r.sentAt)}</span>}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
