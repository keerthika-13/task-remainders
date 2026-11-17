import { useState } from 'react';

const REMINDER_OPTIONS = [
  'No reminder', '5 min before', '10 min before', '15 min before',
  '30 min before', '1 hour before', '2 hours before', '1 day before', 'custom'
];
const PRIORITIES  = ['Low', 'Medium', 'High', 'Urgent'];
const CATEGORIES  = ['Personal', 'Work', 'Study', 'Health', 'Finance', 'Shopping', 'Other'];

export default function TaskModal({ task, onClose, onSave }) {
  const isEdit = !!task?.id;
  const [form, setForm] = useState({
    title:          task?.title          || '',
    description:    task?.description    || '',
    taskDate:       task?.taskDate       || '',
    taskTime:       task?.taskTime       || '',
    priority:       task?.priority       || 'Medium',
    category:       task?.category       || 'Personal',
    reminder:       'No reminder',
    customReminder: '',
  });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);

  const handle = e => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async e => {
    e.preventDefault();
    if (!form.title.trim()) { setError('Title is required'); return; }
    setError(''); setLoading(true);
    try {
      const payload = { ...form };
      if (payload.reminder === 'No reminder') delete payload.reminder;
      await onSave(payload);
      onClose();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save task');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h2>{isEdit ? '✏️ Edit Task' : '➕ New Task'}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={submit}>
          <div className="form-group">
            <label>Title *</label>
            <input className="form-control" name="title" value={form.title} onChange={handle}
              placeholder="What do you need to do?" required />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea className="form-control" name="description" value={form.description} onChange={handle}
              placeholder="Add details..." rows={3} style={{resize:'vertical'}} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Date</label>
              <input className="form-control" type="date" name="taskDate" value={form.taskDate} onChange={handle} />
            </div>
            <div className="form-group">
              <label>Time</label>
              <input className="form-control" type="time" name="taskTime" value={form.taskTime} onChange={handle} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Priority</label>
              <select className="form-control filter-select" name="priority" value={form.priority} onChange={handle} style={{width:'100%'}}>
                {PRIORITIES.map(p => <option key={p}>{p}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Category</label>
              <select className="form-control filter-select" name="category" value={form.category} onChange={handle} style={{width:'100%'}}>
                {CATEGORIES.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
          </div>
          {!isEdit && (
            <div className="form-group">
              <label>Reminder</label>
              <select className="form-control filter-select" name="reminder" value={form.reminder} onChange={handle} style={{width:'100%'}}>
                {REMINDER_OPTIONS.map(r => <option key={r}>{r}</option>)}
              </select>
            </div>
          )}
          {!isEdit && form.reminder === 'custom' && (
            <div className="form-group">
              <label>Custom Reminder Date & Time</label>
              <input className="form-control" type="datetime-local" name="customReminder"
                value={form.customReminder} onChange={handle} />
            </div>
          )}
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? '⏳ Saving...' : isEdit ? '💾 Update' : '➕ Create Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
