import React, { useState } from 'react'
import { useAuth } from '../auth/AuthContext'

export default function UserMenu() {
  const { user, signOut } = useAuth()
  const [open, setOpen] = useState(false)

  if (!user) return null

  return (
    <div style={{ position: 'relative' }}>
      <button
        onClick={() => setOpen(o => !o)}
        style={{
          display: 'flex', alignItems: 'center', gap: 8,
          background: 'transparent', border: '1px solid var(--border)',
          borderRadius: 999, padding: '4px 10px 4px 4px', cursor: 'pointer',
        }}
      >
        {user.pictureUrl ? (
          <img src={user.pictureUrl} alt={user.name || 'User'}
            style={{ width: 26, height: 26, borderRadius: '50%' }} />
        ) : (
          <div style={{
            width: 26, height: 26, borderRadius: '50%',
            background: 'var(--ink)', color: '#fff',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12,
          }}>
            {(user.name || user.email || '?')[0].toUpperCase()}
          </div>
        )}
        <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--ink)' }}>
          {user.name?.split(' ')[0] || 'Account'}
        </span>
      </button>

      {open && (
        <div style={{
          position: 'absolute', top: '110%', right: 0,
          background: 'var(--bg2)', border: '1px solid var(--border)',
          borderRadius: 10, boxShadow: 'var(--shadow-lg)',
          minWidth: 160, zIndex: 200, overflow: 'hidden',
        }}>
          <button
            onClick={() => { setOpen(false); signOut() }}
            style={{
              width: '100%', textAlign: 'left', padding: '10px 14px',
              background: 'none', border: 'none', cursor: 'pointer',
              fontSize: 13, color: 'var(--ink)',
            }}
            onMouseEnter={e => e.currentTarget.style.background = 'var(--bg3)'}
            onMouseLeave={e => e.currentTarget.style.background = 'none'}
          >
            Sign out
          </button>
        </div>
      )}
    </div>
  )
}
