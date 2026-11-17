import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function Dashboard() {
  const [stats, setStats]     = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    api.get('/dashboard/summary')
      .then(r => setStats(r.data))
      .catch(() => setError('Could not load dashboard data.'))
      .finally(() => setLoading(false));
  }, []);

  const getGreeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  };

  if (loading) return <div className="loading"><div className="spinner" /><span>Loading dashboard...</span></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>👋 {getGreeting()}, {user.name?.split(' ')[0] || 'there'}!</h1>
          <p>Here's your task overview for today.</p>
        </div>
        <Link to="/tasks">
          <button className="btn btn-primary">➕ New Task</button>
        </Link>
      </div>

      {error && <div className="error-msg">{error}</div>}

      {stats && (
        <>
          {/* Stat cards */}
          <div className="stats-grid">
            <div className="stat-card today">
              <span className="stat-icon">📅</span>
              <span className="stat-value">{stats.today}</span>
              <span className="stat-label">Today's Tasks</span>
            </div>
            <div className="stat-card pending">
              <span className="stat-icon">⏳</span>
              <span className="stat-value">{stats.pending}</span>
              <span className="stat-label">Pending</span>
            </div>
            <div className="stat-card done">
              <span className="stat-icon">✅</span>
              <span className="stat-value">{stats.completed}</span>
              <span className="stat-label">Completed</span>
            </div>
            <div className="stat-card overdue">
              <span className="stat-icon">🔴</span>
              <span className="stat-value">{stats.overdue}</span>
              <span className="stat-label">Overdue</span>
            </div>
            <div className="stat-card">
              <span className="stat-icon">🚫</span>
              <span className="stat-value">{stats.cancelled}</span>
              <span className="stat-label">Cancelled</span>
            </div>
          </div>

          {/* Upcoming tasks */}
          <div className="card">
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:16}}>
              <h2 style={{fontSize:'1rem'}}>📋 Upcoming Tasks</h2>
              <Link to="/tasks" style={{fontSize:'0.85rem', color:'var(--primary)'}}>View all →</Link>
            </div>

            {!stats.upcoming?.length ? (
              <div className="empty-state" style={{padding:'30px 24px'}}>
                <span className="icon">🎉</span>
                <h3>All clear!</h3>
                <p>No upcoming tasks. Create one to get started.</p>
              </div>
            ) : (
              <div className="tasks-grid">
                {stats.upcoming.map(task => (
                  <div key={task.id} className={`task-card ${task.status.toLowerCase()}`}>
                    <div className="task-body">
                      <div className="task-title">{task.title}</div>
                      {task.description && <div className="task-desc">{task.description}</div>}
                      <div className="task-meta">
                        {task.taskDate && <span className="badge badge-info">📅 {task.taskDate}</span>}
                        {task.taskTime && <span className="badge badge-ghost">🕐 {task.taskTime}</span>}
                        <span className={`badge priority-${task.priority}`}>{task.priority}</span>
                        <span className={`badge status-${task.status}`}>{task.status}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
