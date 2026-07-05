import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { loginWithGoogle, fetchMe } from '../api/auth'
import { getToken, setToken, clearToken } from './tokenStorage'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // On load, if we have a stored token, validate it against /auth/me
  useEffect(() => {
    const existing = getToken()
    if (!existing) {
      setLoading(false)
      return
    }
    fetchMe()
      .then((profile) => {
        if (profile) setUser(profile)
        else clearToken()
      })
      .finally(() => setLoading(false))
  }, [])

  const signInWithGoogleIdToken = useCallback(async (idToken) => {
    const { token, user: profile } = await loginWithGoogle(idToken)
    setToken(token)
    setUser(profile)
    return profile
  }, [])

  const signOut = useCallback(() => {
    clearToken()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, signInWithGoogleIdToken, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
