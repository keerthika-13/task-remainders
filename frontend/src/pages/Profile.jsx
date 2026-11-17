import { useState } from 'react';
import api from '../api/axios';

export default function Profile() {
  const stored = JSON.parse(localStorage.getItem('user') || '{}');
  const [form, setForm] = useState({
    name: stored.name || '',
    mobile: stored.mobile || '',
    telegramChatId: stored.telegramChatId || '',
  });
  const [msg, setMsg]         = useState('');
  const [err, setErr]         = useState('');
  const [loading, setLoading] = useState(false);

  const initials = (form.name || 'U').split(' ').map(w => w[0]).join('').toUpperCase().slice(0,2);

  const handleProfile = async e => {
    e.preventDefault(); setMsg(''); setErr(''); setLoading(true);
    try {
      const { data } = await api.put('/auth/profile', form);
      if (data.success) {
        const updated = data.user ? { ...stored, ...data.user } : { ...stored, ...form };
        localStorage.setItem('user', JSON.stringify(updated));
        setForm({
          name: updated.name || '',
          mobile: updated.mobile || '',
          telegramChatId: updated.telegramChatId || '',
        });
        setMsg('Profile updated successfully!');
      } else { setErr(data.error || 'Failed'); }
    } catch (e) { setErr(e.response?.data?.error || 'Server error'); }
    finally { setLoading(false); }
  };

  return (
    <div className="page" style={{maxWidth: 600}}>
      <div className="page-header">
        <div><h1>👤 Profile</h1><p>Manage your account settings.</p></div>
      </div>

      {/* Avatar */}
      <div className="card" style={{display:'flex', alignItems:'center', gap:20, marginBottom:20}}>
        <div className="user-avatar" style={{width:64, height:64, fontSize:'1.4rem'}}>{initials}</div>
        <div>
          <h2 style={{fontSize:'1.1rem'}}>{stored.name}</h2>
          <p style={{color:'var(--text2)', fontSize:'0.9rem'}}>{stored.email}</p>
        </div>
      </div>

      {/* Edit profile */}
      <div className="card" style={{marginBottom:20}}>
        <h3 style={{marginBottom:20}}>Edit Profile</h3>
        {msg && <div className="success-msg">✅ {msg}</div>}
        {err && <div className="error-msg">⚠️ {err}</div>}
        <form onSubmit={handleProfile}>
          <div className="form-group">
            <label>Full Name</label>
            <input className="form-control" value={form.name}
              onChange={e => setForm({...form, name:e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Email Address</label>
            <input className="form-control" value={stored.email} disabled
              style={{opacity:0.6, cursor:'not-allowed'}} />
          </div>
          <div className="form-group">
            <label>Mobile Number</label>
            <input className="form-control" value={form.mobile}
              onChange={e => setForm({...form, mobile:e.target.value})}
              placeholder="Optional for profile (not used for Telegram delivery)" />
          </div>
          <div className="form-group">
            <label>Telegram Chat ID <span style={{color:'var(--primary)', fontSize:'0.78rem'}}>(required for Telegram reminders)</span></label>
            <input className="form-control" value={form.telegramChatId}
              onChange={e => setForm({...form, telegramChatId:e.target.value})}
              placeholder="Numeric chat id, e.g. 123456789" />
            <p style={{color:'var(--text2)', fontSize:'0.78rem', marginTop:5}}>
              Telegram reminders are sent to your <strong>Telegram chat ID</strong>, not your phone number.
              <br />1) Create/configure a bot token in backend (<code>application.properties</code> or <code>TELEGRAM_BOT_TOKEN</code>)
              <br />2) Open your bot in Telegram and send <code>/start</code>
              <br />3) Open <code>https://api.telegram.org/bot&lt;TOKEN&gt;/getUpdates</code> and copy <code>message.chat.id</code>
              <br />4) Save that numeric ID here, then create a task with a future reminder time.
            </p>
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? '⏳ Saving...' : '💾 Save Changes'}
          </button>
        </form>
      </div>

      {/* Account info */}
      <div className="card">
        <h3 style={{marginBottom:16}}>Account Info</h3>
        <div style={{display:'grid', gap:12}}>
          {[
            {label:'Name',   value: stored.name},
            {label:'Email',  value: stored.email},
            {label:'Mobile', value: stored.mobile || '—'},
          ].map(item => (
            <div key={item.label} style={{display:'flex', justifyContent:'space-between', padding:'10px 0', borderBottom:'1px solid var(--border)'}}>
              <span style={{color:'var(--text2)', fontSize:'0.9rem'}}>{item.label}</span>
              <span style={{color:'var(--text)', fontSize:'0.9rem'}}>{item.value}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
