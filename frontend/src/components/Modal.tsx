import type { ReactNode } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}

export const Modal = ({ isOpen, onClose, title, children }: ModalProps) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />
      <div className="relative glass-panel rounded-2xl w-full max-w-md p-6 shadow-2xl animate-slide-up border border-[var(--color-border-focus)]">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-display font-semibold text-white">{title}</h2>
          <button 
            onClick={onClose}
            className="text-[var(--color-text-muted)] hover:text-white transition-colors cursor-pointer text-xl leading-none"
          >
            &times;
          </button>
        </div>
        {children}
      </div>
    </div>
  );
};
