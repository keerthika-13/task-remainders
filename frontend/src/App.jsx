import { useEffect, useState } from 'react';

export default function App() {
  const [health, setHealth] = useState(null);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const healthResponse = await fetch('/api/health');
        const healthData = await healthResponse.json();
        setHealth(healthData);

        const customerResponse = await fetch('/api/customers');
        const customerData = await customerResponse.json();
        setCustomers(customerData.customers ?? []);
      } catch (error) {
        console.error('Error fetching app data:', error);
        setHealth({ status: 'error', service: 'frontend fallback' });
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  return (
    <main className="page-shell">
      <section className="hero-card">
        <p className="eyebrow">Project starter</p>
        <h1>Task Remainder</h1>
        <p className="subtitle">
          React frontend connected to a plain Java backend with MySQL support.
        </p>

        <div className="status-box">
          <span className="label">Backend status</span>
          {loading ? <strong>Checking...</strong> : <strong>{health?.status ?? 'unknown'}</strong>}
        </div>
      </section>

      <section className="panel">
        <h2>Customer list</h2>
        {customers.length === 0 ? (
          <p>No customer data yet.</p>
        ) : (
          <ul className="customer-list">
            {customers.map((customer) => (
              <li key={customer.id} className="customer-item">
                <div>
                  <strong>{customer.name}</strong>
                  <span>{customer.email}</span>
                </div>
                <span className={`pill ${customer.status}`}>{customer.status}</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
