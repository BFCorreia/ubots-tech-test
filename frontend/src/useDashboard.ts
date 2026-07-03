import { useState, useEffect, useCallback, startTransition } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { fetchDashboardData, type DashboardData } from './api';

export function useDashboard() {
    const [data, setData] = useState<DashboardData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [connected, setConnected] = useState(false);

    const loadData = useCallback(async () => {
        try {
            const result = await fetchDashboardData();
            startTransition(() => {
                setData(result);
                setError(null);
            });
        } catch (err) {
            startTransition(() => {
                setError('Failed to load dashboard data');
            });
        } finally {
            startTransition(() => {
                setLoading(false);
            });
        }
    }, []);

    useEffect(() => {
        loadData();

        const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';
        const client = new Client({
            webSocketFactory: () => new SockJS(wsUrl),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('Connected to STOMP');
                startTransition(() => setConnected(true));
                loadData(); // resync on connect/reconnect
                client.subscribe('/topic/dashboard', () => {
                    loadData();
                });
            },
            onWebSocketClose: () => {
                startTransition(() => setConnected(false));
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
            }
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [loadData]);

    return { data, loading, error, connected, refresh: loadData };
}
