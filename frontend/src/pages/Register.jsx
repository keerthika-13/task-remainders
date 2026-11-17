import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm]       = useState({ name:'', email:'', mobile:'', password:'', confirmPassword:'' });
  const [error, setError]     = useState('');
  const [loading, setLoading] = useState(false);

  const handle = e => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async e => {
    e.preventDefault();
    setError(''); setLoading(true);
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match'); setLoading(false); return;
    }
    try {
      const { data } = await api.post('/auth/register', form);
      if (data.success) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        navigate('/dashboard');
      } else {
        setError(data.error || 'Registration failed');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Server error. Is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="icon">⏰</div>
          <h1>Create Account</h1>
          <p>Join Task Reminder today</p>
        </div>

        {error && <div className="error-msg">⚠️ {error}</div>}

        <form onSubmit={submit}>
          <div className="form-group">
            <label>Full Name</label>
            <input className="form-control" name="name" placeholder="Keerthika" value={form.name} onChange={handle} required />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Email</label>
              <input className="form-control" type="email" name="email" placeholder="you@example.com" value={form.email} onChange={handle} required />
            </div>
            <div className="form-group">
              <label>Mobile Number</label>
              <input className="form-control" name="mobile" placeholder="+91XXXXXXXXXX" value={form.mobile} onChange={handle} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Password</label>
              <input className="form-control" type="password" name="password" placeholder="Min 6 characters" value={form.password} onChange={handle} required />
            </div>
            <div className="form-group">
              <label>Confirm Password</label>
              <input className="form-control" type="password" name="confirmPassword" placeholder="Repeat password" value={form.confirmPassword} onChange={handle} required />
            </div>
          </div>
          <button className="btn btn-primary btn-full" type="submit" disabled={loading}>
            {loading ? '⏳ Creating account...' : '🚀 Create Account'}
          </button>
        </form>

        <p style={{textAlign:'center', marginTop:20, color:'var(--text2)', fontSize:'0.9rem'}}>
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
