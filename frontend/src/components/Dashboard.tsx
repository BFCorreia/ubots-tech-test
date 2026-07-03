import { useState } from 'react';
import { useDashboard } from '../useDashboard';
import { finishTicket, type Team, type Ticket } from '../api';
import { Modal } from './Modal';
import { Simulator } from './Simulator';
import { AddAttendant } from './AddAttendant';
import { TimeElapsed, formatDuration } from './TimeElapsed';

const TEAM_NAMES: Record<Team, string> = {
  CARDS: 'Cartões',
  LOANS: 'Empréstimos',
  OTHER: 'Outros Assuntos'
};

export const Dashboard: React.FC = () => {
  const { data, loading, error, connected } = useDashboard();
  const [isSimulatorOpen, setIsSimulatorOpen] = useState(false);
  const [isAttendantOpen, setIsAttendantOpen] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-xl text-blue-400 animate-pulse font-display">Conectando ao FlowPay...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-red-400 glass-panel p-6 rounded-xl border border-red-500/30">
          <h2 className="font-display text-xl mb-2">Ops, algo deu errado</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!data) return null;

  const getTeamStats = (team: Team) => {
    const teamAttendants = data.attendants.filter(a => a.team === team);
    const inProgress = data.tickets.filter(t => t.team === team && t.status === 'IN_PROGRESS');
    const waiting = data.tickets.filter(t => t.team === team && t.status === 'WAITING');
    
    return {
      attendants: teamAttendants,
      inProgressCount: inProgress.length,
      waiting,
    };
  };

  return (
    <>
    <div className="p-8 max-w-7xl mx-auto animate-slide-up">
      <header className="mb-10 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-display font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-pink-500">
            FlowPay Hub
          </h1>
          <p className="text-[var(--color-text-secondary)] mt-2">Visão em tempo real do atendimento</p>
        </div>
        <div className="flex gap-4 text-sm text-[var(--color-text-muted)] items-center">
          <div className="flex items-center gap-2 mr-4">
            <span className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></span>
            {connected ? 'Online' : 'Offline (Reconectando...)'}
          </div>
          
          <button 
            onClick={() => setIsAttendantOpen(true)}
            className="px-4 py-2 bg-pink-600/20 text-pink-400 hover:bg-pink-600 hover:text-white transition-colors rounded-lg font-semibold cursor-pointer"
          >
            + Novo Atendente
          </button>
          
          <button 
            onClick={() => setIsSimulatorOpen(true)}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white transition-colors rounded-lg font-semibold cursor-pointer shadow-lg shadow-blue-500/20"
          >
            Criar Chamado
          </button>
        </div>
      </header>

      {/* Global Stats */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        <div className="glass-panel p-4 rounded-xl text-center">
          <div className="text-[var(--color-text-muted)] text-xs uppercase tracking-wider mb-1">Em Atendimento</div>
          <div className="text-2xl font-display font-semibold text-white">{data.stats.totalInProgress}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl text-center">
          <div className="text-[var(--color-text-muted)] text-xs uppercase tracking-wider mb-1">Na Fila (Espera)</div>
          <div className="text-2xl font-display font-semibold text-white">{data.stats.totalWaiting}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl text-center">
          <div className="text-[var(--color-text-muted)] text-xs uppercase tracking-wider mb-1">Finalizados</div>
          <div className="text-2xl font-display font-semibold text-white">{data.stats.totalFinished}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl text-center">
          <div className="text-[var(--color-text-muted)] text-[10px] uppercase tracking-wider mb-1">T. Médio (Fila Atual)</div>
          <div className="text-2xl font-display font-semibold text-pink-400">{formatDuration(data.stats.avgWaitTimeSeconds)}</div>
        </div>
        <div className="glass-panel p-4 rounded-xl text-center">
          <div className="text-[var(--color-text-muted)] text-[10px] uppercase tracking-wider mb-1">T. Médio (SLA Histórico)</div>
          <div className="text-2xl font-display font-semibold text-blue-400">{formatDuration(data.stats.slaResponseTimeSeconds)}</div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {(Object.keys(TEAM_NAMES) as Team[]).map(team => {
          const stats = getTeamStats(team);
          return (
            <section key={team} className="glass-panel rounded-2xl p-6 flex flex-col gap-6 hover-glow">
              <header className="flex justify-between items-center pb-4 border-b border-[var(--color-border-subtle)]">
                <h2 className="text-xl font-display font-semibold">{TEAM_NAMES[team]}</h2>
                <div className="flex gap-2 items-center">
                  {stats.attendants.length === 0 && stats.waiting.length > 0 && (
                    <span className="text-xs font-medium px-2 py-1 rounded-full bg-red-600/20 text-red-400 border border-red-600/30">
                      Sem atendentes
                    </span>
                  )}
                  <div className="px-3 py-1 bg-blue-500/10 text-blue-400 rounded-full text-xs font-semibold">
                    {stats.waiting.length} na fila
                  </div>
                </div>
              </header>

              <div className="flex-1">
                <h3 className="text-xs uppercase tracking-wider text-[var(--color-text-muted)] mb-3 font-semibold">Atendentes</h3>
                {stats.attendants.length === 0 ? (
                  <p className="text-sm text-[var(--color-text-muted)] italic">Nenhum atendente cadastrado</p>
                ) : (
                  <ul className="flex flex-col gap-3">
                    {stats.attendants.map(att => (
                      <li key={att.id} className="flex justify-between items-center bg-black/20 p-3 rounded-lg border border-[var(--color-border-subtle)]">
                        <span className="font-medium">{att.name}</span>
                        <div className="flex items-center gap-2">
                          <span className="text-xs text-[var(--color-text-secondary)]">{att.activeTickets}/3</span>
                          <span className={`w-2 h-2 rounded-full ${att.activeTickets >= 3 ? 'bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.8)]' : 'bg-green-500'}`}></span>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div>
                <h3 className="text-xs uppercase tracking-wider text-[var(--color-text-muted)] mb-3 font-semibold">Fila de Espera</h3>
                {stats.waiting.length === 0 ? (
                  <p className="text-sm text-[var(--color-text-muted)] italic">Fila vazia. Tudo limpo!</p>
                ) : (
                  <ul className="flex flex-col gap-2 max-h-40 overflow-y-auto pr-2">
                    {stats.waiting.map(ticket => (
                      <li key={ticket.id} className="text-sm p-2 rounded bg-black/20 border-l-2 border-pink-500 flex justify-between items-center">
                        <div className="overflow-hidden">
                          <div className="font-medium truncate flex items-center gap-2">
                             {ticket.clientName}
                             <button onClick={() => setSelectedTicket(ticket)} className="text-blue-400 hover:text-blue-300 p-1 flex-shrink-0 cursor-pointer" title="Ver detalhes">
                               <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                             </button>
                          </div>
                          <div className="text-xs text-[var(--color-text-secondary)] truncate">{ticket.subject}</div>
                        </div>
                        <div className="text-xs text-[var(--color-text-muted)] shrink-0 pl-2">
                          <TimeElapsed start={ticket.createdAt} />
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </section>
          );
        })}
      </div>
      
      <div className="mt-10">
        <h2 className="text-xl font-display font-semibold mb-4">Atendimentos em Andamento</h2>
        <div className="glass-panel p-4 rounded-xl overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="text-xs uppercase tracking-wider text-[var(--color-text-muted)] border-b border-[var(--color-border-subtle)]">
                <th className="p-3">Cliente</th>
                <th className="p-3">Assunto</th>
                <th className="p-3">Time</th>
                <th className="p-3">Atendente</th>
                <th className="p-3">Tempo</th>
                <th className="p-3 text-right">Ação</th>
              </tr>
            </thead>
            <tbody>
              {data.tickets.filter(t => t.status === 'IN_PROGRESS').length === 0 ? (
                <tr><td colSpan={6} className="p-4 text-center text-[var(--color-text-muted)]">Nenhum atendimento em andamento</td></tr>
              ) : (
                data.tickets.filter(t => t.status === 'IN_PROGRESS').map(t => (
                  <tr key={t.id} className="border-b border-[var(--color-border-subtle)] last:border-0 hover:bg-black/20">
                    <td className="p-3 font-medium">{t.clientName}</td>
                    <td className="p-3">
                      <div className="text-[var(--color-text-secondary)]">{t.subject}</div>
                      <button onClick={() => setSelectedTicket(t)} className="text-[10px] text-blue-400 hover:text-blue-300 mt-1 uppercase tracking-wider font-semibold cursor-pointer">Ver detalhes</button>
                    </td>
                    <td className="p-3 text-[var(--color-text-secondary)]">{TEAM_NAMES[t.team]}</td>
                    <td className="p-3 text-blue-400">{t.attendant?.name}</td>
                    <td className="p-3 text-[var(--color-text-muted)]">
                      {t.startedAt ? <TimeElapsed start={t.startedAt} /> : '-'}
                    </td>
                    <td className="p-3 text-right">
                      <button 
                        onClick={() => finishTicket(t.id).catch(console.error)}
                        className="px-3 py-1 bg-pink-500/20 text-pink-400 hover:bg-pink-500 hover:text-white transition-colors rounded-md text-xs font-semibold cursor-pointer"
                      >
                        Finalizar
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="mt-10">
        <h2 className="text-xl font-display font-semibold mb-4">Últimos Atendimentos Finalizados</h2>
        <div className="glass-panel p-4 rounded-xl overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="text-xs uppercase tracking-wider text-[var(--color-text-muted)] border-b border-[var(--color-border-subtle)]">
                <th className="p-3">Cliente</th>
                <th className="p-3">Assunto</th>
                <th className="p-3">Time</th>
                <th className="p-3">Atendente</th>
                <th className="p-3">Duração</th>
              </tr>
            </thead>
            <tbody>
              {data.stats.recentFinished.length === 0 ? (
                <tr><td colSpan={4} className="p-4 text-center text-[var(--color-text-muted)]">Nenhum atendimento finalizado ainda</td></tr>
              ) : (
                data.stats.recentFinished.map(t => (
                  <tr key={t.id} className="border-b border-[var(--color-border-subtle)] last:border-0 hover:bg-black/20 opacity-75">
                    <td className="p-3 font-medium text-[var(--color-text-secondary)]">{t.clientName}</td>
                    <td className="p-3">
                      <div className="text-[var(--color-text-muted)]">{t.subject}</div>
                      <button onClick={() => setSelectedTicket(t)} className="text-[10px] text-blue-400 hover:text-blue-300 mt-1 uppercase tracking-wider font-semibold opacity-70 cursor-pointer">Ver detalhes</button>
                    </td>
                    <td className="p-3 text-[var(--color-text-muted)]">{TEAM_NAMES[t.team]}</td>
                    <td className="p-3 text-[var(--color-text-muted)]">{t.attendant?.name}</td>
                    <td className="p-3 text-[var(--color-text-muted)]">
                      {t.startedAt && t.finishedAt ? 
                        formatDuration(Math.round((new Date(t.finishedAt).getTime() - new Date(t.startedAt).getTime()) / 1000)) 
                        : '-'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
      {/* Modals */}
      <Modal 
        isOpen={isSimulatorOpen} 
        onClose={() => setIsSimulatorOpen(false)} 
        title="Criar Chamado"
      >
        <Simulator onSuccess={() => setIsSimulatorOpen(false)} />
      </Modal>

      <Modal 
        isOpen={isAttendantOpen} 
        onClose={() => setIsAttendantOpen(false)} 
        title="Novo Atendente"
      >
        <AddAttendant onSuccess={() => setIsAttendantOpen(false)} />
      </Modal>

      <Modal 
        isOpen={!!selectedTicket} 
        onClose={() => setSelectedTicket(null)} 
        title="Detalhes do Chamado"
      >
        {selectedTicket && (
          <div className="flex flex-col gap-6">
            <div>
              <div className="text-xs text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Cliente</div>
              <div className="font-medium text-white">{selectedTicket.clientName}</div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <div className="text-xs text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Assunto</div>
                <div className="text-white">{selectedTicket.subject}</div>
              </div>
              <div>
                <div className="text-xs text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Time</div>
                <div className="text-white">{TEAM_NAMES[selectedTicket.team]}</div>
              </div>
            </div>
            <div className="grid grid-cols-3 gap-4 border-y border-[var(--color-border-subtle)] py-4">
              <div>
                <div className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Criado em</div>
                <div className="text-sm text-[var(--color-text-secondary)]">{new Date(selectedTicket.createdAt).toLocaleTimeString()}</div>
              </div>
              {selectedTicket.startedAt && (
                <div>
                  <div className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Iniciado em</div>
                  <div className="text-sm text-blue-400">{new Date(selectedTicket.startedAt).toLocaleTimeString()}</div>
                </div>
              )}
              {selectedTicket.finishedAt && (
                <div>
                  <div className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Finalizado em</div>
                  <div className="text-sm text-pink-400">{new Date(selectedTicket.finishedAt).toLocaleTimeString()}</div>
                </div>
              )}
            </div>
            <div>
              <div className="text-xs text-[var(--color-text-muted)] uppercase tracking-wider mb-1">Descrição do Problema</div>
              <div className="bg-black/30 p-4 rounded-lg border border-[var(--color-border-subtle)] text-slate-300 text-sm whitespace-pre-wrap max-h-48 overflow-y-auto">
                {selectedTicket.description}
              </div>
            </div>
          </div>
        )}
      </Modal>
    </>
  );
};
