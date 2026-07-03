export type Team = 'CARDS' | 'LOANS' | 'OTHER';

export interface Attendant {
    id: string;
    name: string;
    team: Team;
    activeTickets: number;
}

export interface Ticket {
    id: string;
    clientName: string;
    subject: string;
    description: string;
    team: Team;
    status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED';
    createdAt: string;
    startedAt: string | null;
    finishedAt: string | null;
    attendant: Attendant | null;
}

export interface DashboardStats {
    totalInProgress: number;
    totalWaiting: number;
    totalFinished: number;
    avgWaitTimeSeconds: number;
    slaResponseTimeSeconds: number;
    recentFinished: Ticket[];
}

export interface DashboardData {
    attendants: Attendant[];
    tickets: Ticket[];
    stats: DashboardStats;
}

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const fetchDashboardData = async (): Promise<DashboardData> => {
    const res = await fetch(`${API_BASE}/dashboard`);
    if (!res.ok) throw new Error('Failed to fetch dashboard');
    return res.json();
};

export const createTicket = async (clientName: string, subject: string, description: string) => {
    const res = await fetch(`${API_BASE}/tickets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ clientName, subject, description })
    });
    if (!res.ok) throw new Error('Failed to create ticket');
    return res.json();
};

export const finishTicket = async (ticketId: string) => {
    const res = await fetch(`${API_BASE}/tickets/${ticketId}/finish`, {
        method: 'POST'
    });
    if (!res.ok) throw new Error('Failed to finish ticket');
    return res.json();
};
