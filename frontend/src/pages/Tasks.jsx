import { useEffect, useState, useCallback } from 'react';
import api from '../api/axios';
import TaskModal from '../components/TaskModal';

const STATUSES    = ['', 'Pending', 'Completed', 'Overdue', 'Cancelled'];
const CATEGORIES  = ['', 'Personal', 'Work', 'Study', 'Health', 'Finance', 'Shopping', 'Other'];
const PRIORITIES  = ['', 'Low', 'Medium', 'High', 'Urgent'];

export default function Tasks() {
  const [tasks, setTasks]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');
  const [showModal, setModal]   = useState(false);
  const [editTask, setEditTask]  = useState(null);
  const [filters, setFilters]   = useState({ search:'', status:'', category:'', priority:'' });
  const [confirm, setConfirm]   = useState(null); // { id, title }

  const loadTasks = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (filters.search)   params.set('search',   filters.search);
      if (filters.status)   params.set('status',   filters.status);
      if (filters.category) params.set('category', filters.category);
      if (filters.priority) params.set('priority', filters.priority);
      const { data } = await api.get(`/tasks?${params}`);
      setTasks(data.tasks || []);
    } catch { setError('Failed to load tasks'); }
    finally { setLoading(false); }
  }, [filters]);

  useEffect(() => { loadTasks(); }, [loadTasks]);

  const handleCreate = async (payload) => {
    await api.post('/tasks', payload);
    loadTasks();
  };

  const handleUpdate = async (payload) => {
    await api.put(`/tasks/${editTask.id}`, payload);
    loadTasks();
  };

  const handleDelete = async (id) => {
    await api.delete(`/tasks/${id}`);
    setConfirm(null);
    loadTasks();
  };

  const handleComplete = async (id) => {
    await api.patch(`/tasks/${id}/complete`);
    loadTasks();
  };

  const handleCancel = async (id) => {
    await api.patch(`/tasks/${id}/cancel`);
    loadTasks();
  };

  const openEdit = (task) => { setEditTask(task); setModal(true); };
  const openNew  = ()     => { setEditTask(null);  setModal(true); };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>✅ Tasks</h1>
          <p>Manage all your tasks in one place.</p>
        </div>
        <button className="btn btn-primary" onClick={openNew}>➕ New Task</button>
      </div>

      {/* Filters */}
      <div className="filters-bar">
        <div className="search-box">
          🔍
          <input placeholder="Search tasks..."
            value={filters.search}
            onChange={e => setFilters({ ...filters, search: e.target.value })} />
        </div>
        <select className="filter-select" value={filters.status}
          onChange={e => setFilters({ ...filters, status: e.target.value })}>
          {STATUSES.map(s => <option key={s} value={s}>{s || 'All Status'}</option>)}
        </select>
        <select className="filter-select" value={filters.priority}
          onChange={e => setFilters({ ...filters, priority: e.target.value })}>
          {PRIORITIES.map(p => <option key={p} value={p}>{p || 'All Priority'}</option>)}
        </select>
        <select className="filter-select" value={filters.category}
          onChange={e => setFilters({ ...filters, category: e.target.value })}>
          {CATEGORIES.map(c => <option key={c} value={c}>{c || 'All Category'}</option>)}
        </select>
      </div>

      {error && <div className="error-msg">{error}</div>}

      {loading ? (
        <div className="loading"><div className="spinner" /><span>Loading tasks...</span></div>
      ) : tasks.length === 0 ? (
        <div className="empty-state">
          <span className="icon">📋</span>
          <h3>No tasks found</h3>
          <p>Create your first task to get started!</p>
          <button className="btn btn-primary" onClick={openNew}>➕ Create Task</button>
        </div>
      ) : (
        <div className="tasks-grid">
          {tasks.map(task => (
            <div key={task.id} className={`task-card ${task.status === 'Completed' ? 'completed' : ''} ${task.status === 'Overdue' ? 'overdue' : ''}`}>
              <div
                className={`task-check ${task.status === 'Completed' ? 'checked' : ''}`}
                onClick={() => task.status !== 'Completed' && handleComplete(task.id)}
                title="Mark complete"
              />
              <div className="task-body">
                <div className={`task-title ${task.status === 'Completed' ? 'done' : ''}`}>{task.title}</div>
                {task.description && <div className="task-desc">{task.description}</div>}
                <div className="task-meta">
                  {task.taskDate && <span className="badge badge-info">📅 {task.taskDate}</span>}
                  {task.taskTime && <span className="badge badge-ghost">🕐 {task.taskTime.slice(0,5)}</span>}
                  <span className={`badge priority-${task.priority}`}>{task.priority}</span>
                  <span className={`badge status-${task.status}`}>{task.status}</span>
                  <span className="badge badge-ghost">{task.category}</span>
                </div>
              </div>
              <div className="task-actions">
                {task.status === 'Pending' && (
                  <button className="btn btn-sm btn-ghost btn-icon" title="Edit" onClick={() => openEdit(task)}>✏️</button>
                )}
                {task.status === 'Pending' && (
                  <button className="btn btn-sm btn-ghost btn-icon" title="Cancel" onClick={() => handleCancel(task.id)}>🚫</button>
                )}
                <button className="btn btn-sm btn-danger btn-icon" title="Delete"
                  onClick={() => setConfirm({ id: task.id, title: task.title })}>🗑️</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Task Modal */}
      {showModal && (
        <TaskModal
          task={editTask}
          onClose={() => { setModal(false); setEditTask(null); }}
          onSave={editTask ? handleUpdate : handleCreate}
        />
      )}

      {/* Delete Confirm Dialog */}
      {confirm && (
        <div className="modal-overlay">
          <div className="modal" style={{maxWidth:380}}>
            <div className="modal-header">
              <h2>🗑️ Delete Task</h2>
              <button className="modal-close" onClick={() => setConfirm(null)}>✕</button>
            </div>
            <p style={{color:'var(--text2)'}}>Are you sure you want to delete "<strong style={{color:'var(--text)'}}>{confirm.title}</strong>"? This cannot be undone.</p>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setConfirm(null)}>Cancel</button>
              <button className="btn btn-danger" onClick={() => handleDelete(confirm.id)}>🗑️ Delete</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
