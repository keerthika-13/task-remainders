import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../api/axios';

export default function Layout() {
  const navigate = useNavigate();
  const [user, setUser]       = useState(JSON.parse(localStorage.getItem('user') || '{}'));
  const [unread, setUnread]   = useState(0);
  const [sideOpen, setSide]   = useState(false);

  useEffect(() => {
    api.get('/notifications').then(r => setUnread(r.data.unreadCount || 0)).catch(() => {});
    const interval = setInterval(() => {
      api.get('/notifications').then(r => setUnread(r.data.unreadCount || 0)).catch(() => {});
    }, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleLogout = async () => {
    try { await api.post('/auth/logout'); } catch {}
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const initials = (user.name || 'U').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);

  const navLinks = [
    { to: '/dashboard',     icon: '🏠', label: 'Dashboard' },
    { to: '/tasks',         icon: '✅', label: 'Tasks' },
    { to: '/calendar',      icon: '📅', label: 'Calendar' },
    { to: '/reminders',     icon: '🔔', label: 'Reminders' },
    { to: '/notifications', icon: '📬', label: 'Notifications', badge: unread },
    { to: '/profile',       icon: '👤', label: 'Profile' },
  ];

  return (
    <div className="app-layout">
      {/* Overlay for mobile */}
      {sideOpen && <div style={{position:'fixed',inset:0,background:'rgba(0,0,0,0.5)',zIndex:99}} onClick={() => setSide(false)} />}

      <aside className={`sidebar ${sideOpen ? 'open' : ''}`}>
        <div className="sidebar-logo">
          <span>⏰</span>
          <div>
            <h2>Task Reminder</h2>
            <p>Stay on track</p>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-label">Main Menu</div>
          {navLinks.map(link => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              onClick={() => setSide(false)}
            >
              <span className="icon">{link.icon}</span>
              {link.label}
              {link.badge > 0 && <span className="badge">{link.badge}</span>}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{initials}</div>
            <div className="user-info-text">
              <strong>{user.name || 'User'}</strong>
              <span>{user.email || ''}</span>
            </div>
          </div>
          <button className="btn btn-ghost btn-full" style={{marginTop:8}} onClick={handleLogout}>
            🚪 Logout
          </button>
        </div>
      </aside>

      <div className="main-content">
        <header className="topbar">
          <button className="btn btn-icon btn-ghost" style={{display:'none'}} onClick={() => setSide(s => !s)}>☰</button>
          <span className="topbar-title">⏰ Task Reminder</span>
          <div className="topbar-actions">
            <NavLink to="/notifications">
              <button className="notif-btn">
                📬
                {unread > 0 && <span className="notif-dot" />}
              </button>
            </NavLink>
            <div className="user-avatar" style={{width:34,height:34,fontSize:'0.85rem'}}>{initials}</div>
          </div>
        </header>
        <Outlet />
      </div>
    </div>
  );
}
