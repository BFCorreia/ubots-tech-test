import { useState, startTransition } from 'react';
import { type Team } from '../api';
import { Select } from './Select';

const API_BASE = 'http://localhost:8080/api';

interface AddAttendantProps {
  onSuccess?: () => void;
}

export const AddAttendant = ({ onSuccess }: AddAttendantProps) => {
  const [name, setName] = useState('');
  const [team, setTeam] = useState<Team>('CARDS');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    
    startTransition(() => setLoading(true));
    try {
      setError('');
      const res = await fetch(`${API_BASE}/attendants`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, team })
      });
      if (!res.ok) throw new Error('Falha');
      startTransition(() => setName(''));
      if (onSuccess) onSuccess();
    } catch (err) {
      setError('Falha ao criar atendente. Tente novamente.');
      console.error(err);
    } finally {
      startTransition(() => setLoading(false));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="block text-xs uppercase tracking-wider text-[var(--color-text-muted)] mb-1">Nome</label>
        <input 
          type="text" 
          value={name}
          onChange={e => setName(e.target.value)}
          className="w-full bg-black/30 border border-[var(--color-border-subtle)] rounded-lg p-2 text-sm focus:outline-none focus:border-blue-500 transition-colors text-white"
          placeholder="Ex: Maria"
          required
        />
      </div>
      <div>
        <label className="block text-xs uppercase tracking-wider text-[var(--color-text-muted)] mb-1">Time</label>
        <Select 
          value={team}
          onChange={(val) => setTeam(val as Team)}
          options={[
            { value: 'CARDS', label: 'Cartões' },
            { value: 'LOANS', label: 'Empréstimos' },
            { value: 'OTHER', label: 'Outros Assuntos' }
          ]}
        />
      </div>
      {error && <div className="text-sm text-red-400 bg-red-400/10 p-2 rounded">{error}</div>}
      <button 
        type="submit" 
        disabled={loading}
        className="w-full bg-pink-600 hover:bg-pink-500 disabled:opacity-50 text-white font-medium py-2 rounded-lg transition-colors mt-2 cursor-pointer"
      >
        {loading ? 'Adicionando...' : 'Adicionar Atendente'}
      </button>
    </form>
  );
};
