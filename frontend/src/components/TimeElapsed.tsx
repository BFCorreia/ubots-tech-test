import { useState, useEffect } from 'react';

export const TimeElapsed = ({ start }: { start: string }) => {
    const [elapsed, setElapsed] = useState('');

    useEffect(() => {
        if (!start) return;
        const calculate = () => {
            const diff = Math.floor((Date.now() - new Date(start).getTime()) / 1000);
            if (diff < 60) setElapsed(`${diff}s`);
            else {
                const mins = Math.floor(diff / 60);
                const secs = diff % 60;
                setElapsed(`${mins}m ${secs}s`);
            }
        };
        calculate();
        const interval = setInterval(calculate, 1000);
        return () => clearInterval(interval);
    }, [start]);

    return <span>{elapsed || '...'}</span>;
};

export const formatDuration = (seconds: number) => {
    if (seconds === 0) return '0s';
    if (seconds < 60) return `${Math.floor(seconds)}s`;
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}m ${secs}s`;
};
