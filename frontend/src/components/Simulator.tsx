import { useState, startTransition } from 'react';
import { createTicket } from '../api';
import { Select } from './Select';

interface SimulatorProps {
  onSuccess?: () => void;
}

export const Simulator = ({ onSuccess }: SimulatorProps) => {
  const [name, setName] = useState('');
  const [subject, setSubject] = useState('Problemas com cartão');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !subject.trim() || !description.trim()) return;
    
    startTransition(() => {
        setLoading(true);
    });
    
    try {
      setError('');
      await createTicket(name, subject, description);
      startTransition(() => {
          setName('');
          setSubject('Problemas com cartão');
          setDescription('');
      });
      if (onSuccess) onSuccess();
    } catch (err) {
      setError('Falha ao criar chamado. Tente novamente.');
      console.error(err);
    } finally {
      startTransition(() => {
          setLoading(false);
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="block text-xs uppercase tracking-wider text-[var(--color-text-muted)] mb-1">Nome do Cliente</label>
        <input 
          type="text" 
          value={name}
          onChange={e => setName(e.target.value)}
          className="w-full bg-black/30 border border-[var(--color-border-subtle)] rounded-lg p-2 text-sm focus:outline-none focus:border-blue-500 transition-colors text-white"
          placeholder="Ex: João Silva"
          required
        />
      </div>
      <div>
        <label className="block text-xs uppercase tracking-wider text-[var(--color-text-muted)] mb-1">Motivo (Assunto)</label>
        <Select 
          value={subject}
          onChange={setSubject}
          options={[
            { value: 'Problemas com cartão', label: 'Problemas com cartão' },
            { value: 'Contratação de empréstimo', label: 'Contratação de empréstimo' },
            { value: 'Outro assunto', label: 'Outro assunto' }
          ]}
        />
      </div>
      <div>
        <div className="flex justify-between items-end mb-1">
          <label className="block text-xs uppercase tracking-wider text-[var(--color-text-muted)]">Descrição</label>
          <span className="text-[10px] text-[var(--color-text-muted)]">{description.length}/1000</span>
        </div>
        <textarea 
          value={description}
          onChange={e => setDescription(e.target.value)}
          maxLength={1000}
          className="w-full bg-black/30 border border-[var(--color-border-subtle)] rounded-lg p-2 text-sm focus:outline-none focus:border-blue-500 transition-colors text-white resize-none h-24"
          placeholder="Descreva o problema com detalhes..."
          required
        />
      </div>
      {error && <div className="text-sm text-red-400 bg-red-400/10 p-2 rounded">{error}</div>}
      <button 
        type="submit" 
        disabled={loading}
        className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-medium py-2 rounded-lg transition-colors mt-2 cursor-pointer"
      >
        {loading ? 'Enviando...' : 'Criar Chamado'}
      </button>
    </form>
  );
};
