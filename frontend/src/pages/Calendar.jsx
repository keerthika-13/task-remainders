import { useEffect, useState } from 'react';
import api from '../api/axios';

const DAYS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MONTHS = ['January','February','March','April','May','June',
                'July','August','September','October','November','December'];

export default function Calendar() {
  const today  = new Date();
  const [year, setYear]   = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState(null); // date string
  const [dayTasks, setDayTasks] = useState([]);

  useEffect(() => {
    setLoading(true);
    api.get(`/calendar?year=${year}&month=${month}`)
      .then(r => setTasks(r.data.tasks || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [year, month]);

  const prevMonth = () => {
    if (month === 1) { setMonth(12); setYear(y => y - 1); }
    else setMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (month === 12) { setMonth(1); setYear(y => y + 1); }
    else setMonth(m => m + 1);
  };

  // Build calendar grid
  const firstDay = new Date(year, month - 1, 1).getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const daysInPrev  = new Date(year, month - 1, 0).getDate();

  const cells = [];
  for (let i = firstDay - 1; i >= 0; i--)
    cells.push({ day: daysInPrev - i, thisMonth: false });
  for (let d = 1; d <= daysInMonth; d++)
    cells.push({ day: d, thisMonth: true });
  while (cells.length % 7 !== 0)
    cells.push({ day: cells.length - daysInMonth - firstDay + 1, thisMonth: false });

  const tasksByDate = {};
  tasks.forEach(t => {
    if (!tasksByDate[t.taskDate]) tasksByDate[t.taskDate] = [];
    tasksByDate[t.taskDate].push(t);
  });

  const pad = n => String(n).padStart(2, '0');

  const handleDayClick = (cell) => {
    if (!cell.thisMonth) return;
    const dateStr = `${year}-${pad(month)}-${pad(cell.day)}`;
    setSelected(dateStr);
    setDayTasks(tasksByDate[dateStr] || []);
  };

  const isToday = (cell) =>
    cell.thisMonth &&
    cell.day === today.getDate() &&
    month === today.getMonth() + 1 &&
    year === today.getFullYear();

  const statusColor = s => ({
    Pending:'#fcd34d', Completed:'#86efac', Overdue:'#fca5a5', Cancelled:'#94a3b8'
  }[s] || '#94a3b8');

  return (
    <div className="page">
      <div className="page-header">
        <div><h1>📅 Calendar</h1><p>View your tasks by date.</p></div>
      </div>

      <div className="card" style={{marginBottom: 20}}>
        <div className="cal-nav">
          <button className="btn btn-ghost btn-sm" onClick={prevMonth}>‹ Prev</button>
          <h2>{MONTHS[month - 1]} {year}</h2>
          <button className="btn btn-ghost btn-sm" onClick={nextMonth}>Next ›</button>
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : (
          <div className="calendar-grid">
            {DAYS.map(d => <div key={d} className="cal-header-cell">{d}</div>)}
            {cells.map((cell, i) => {
              const dateStr = cell.thisMonth ? `${year}-${pad(month)}-${pad(cell.day)}` : null;
              const cellTasks = dateStr ? (tasksByDate[dateStr] || []) : [];
              return (
                <div key={i}
                  className={`cal-day ${!cell.thisMonth ? 'other-month' : ''} ${isToday(cell) ? 'today' : ''}`}
                  onClick={() => handleDayClick(cell)}
                  style={{outline: selected === dateStr ? '2px solid var(--primary)' : undefined}}
                >
                  <div className="day-num">{cell.day}</div>
                  {cellTasks.slice(0, 3).map(t => (
                    <div key={t.id} className="cal-task-dot"
                      style={{background: `${statusColor(t.status)}22`, color: statusColor(t.status)}}>
                      {t.title}
                    </div>
                  ))}
                  {cellTasks.length > 3 && (
                    <div style={{fontSize:'0.65rem', color:'var(--text2)', marginTop:2}}>+{cellTasks.length - 3} more</div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Day detail panel */}
      {selected && (
        <div className="card">
          <h3 style={{marginBottom:14}}>📋 Tasks for {selected}</h3>
          {dayTasks.length === 0 ? (
            <p style={{color:'var(--text2)'}}>No tasks on this day.</p>
          ) : (
            <div className="tasks-grid">
              {dayTasks.map(t => (
                <div key={t.id} className="task-card">
                  <div className="task-body">
                    <div className="task-title">{t.title}</div>
                    {t.description && <div className="task-desc">{t.description}</div>}
                    <div className="task-meta">
                      {t.taskTime && <span className="badge badge-ghost">🕐 {t.taskTime.slice(0,5)}</span>}
                      <span className={`badge priority-${t.priority}`}>{t.priority}</span>
                      <span className={`badge status-${t.status}`}>{t.status}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
