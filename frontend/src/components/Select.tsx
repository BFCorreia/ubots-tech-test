import { useState, useRef, useEffect } from 'react';

interface Option {
  value: string;
  label: string;
}

interface SelectProps {
  value: string;
  onChange: (val: string) => void;
  options: Option[];
}

export const Select = ({ value, onChange, options }: SelectProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const selectedOption = options.find(o => o.value === value) || options[0];

  return (
    <div className="relative w-full" ref={containerRef}>
      <div 
        className="w-full bg-black/30 border border-[var(--color-border-subtle)] rounded-lg p-2 text-sm text-white cursor-pointer flex justify-between items-center hover:border-blue-500/50 transition-colors"
        onClick={() => setIsOpen(!isOpen)}
      >
        <span>{selectedOption?.label}</span>
        <svg className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </div>
      
      {isOpen && (
        <div className="absolute z-50 w-full mt-2 bg-[#0f172a] border border-[var(--color-border-subtle)] rounded-lg shadow-xl shadow-black/50 overflow-hidden origin-top">
          {options.map(opt => (
            <div
              key={opt.value}
              className={`p-3 text-sm cursor-pointer transition-colors hover:bg-blue-500/20 ${opt.value === value ? 'text-blue-400 bg-blue-900/20 font-medium' : 'text-slate-300'}`}
              onClick={() => {
                onChange(opt.value);
                setIsOpen(false);
              }}
            >
              {opt.label}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
