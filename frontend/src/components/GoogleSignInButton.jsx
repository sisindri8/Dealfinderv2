import React, { useEffect, useRef } from 'react'
import { useAuth } from '../auth/AuthContext'

const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID

export default function GoogleSignInButton() {
  const { signInWithGoogleIdToken } = useAuth()
  const buttonRef = useRef(null)

  useEffect(() => {
    if (!CLIENT_ID) {
      console.warn('VITE_GOOGLE_CLIENT_ID is not set — Google Sign-In button will not render.')
      return
    }

    function render() {
      if (!window.google?.accounts?.id || !buttonRef.current) return
      window.google.accounts.id.initialize({
        client_id: CLIENT_ID,
        callback: async ({ credential }) => {
          try {
            await signInWithGoogleIdToken(credential)
          } catch (e) {
            console.error('Google sign-in failed:', e)
          }
        },
      })
      window.google.accounts.id.renderButton(buttonRef.current, {
        theme: 'outline',
        size: 'medium',
        shape: 'pill',
        text: 'signin',
      })
    }

    // The GSI script loads async — poll briefly until it's ready.
    if (window.google?.accounts?.id) {
      render()
    } else {
      const interval = setInterval(() => {
        if (window.google?.accounts?.id) {
          clearInterval(interval)
          render()
        }
      }, 100)
      return () => clearInterval(interval)
    }
  }, [signInWithGoogleIdToken])

  if (!CLIENT_ID) return null
  // Fixed dimensions matching Google's rendered 'medium' pill button so the
  // header doesn't shift when the button pops in after the async script loads.
  return <div ref={buttonRef} style={{ width: 175, height: 40 }} />
}