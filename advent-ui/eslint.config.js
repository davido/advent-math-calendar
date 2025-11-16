import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import prettier from 'eslint-config-prettier';
import { defineConfig, globalIgnores } from 'eslint/config';

export default defineConfig([
  // Globale Ignorier-Liste
  globalIgnores(['dist', 'node_modules']),

  // Basis-JavaScript-Regeln
  js.configs.recommended,

  // TypeScript-Empfehlungen (liefert ein Array von Configs)
  ...tseslint.configs.recommended,

  // Unsere Projekt-spezifische TS/TSX-Konfiguration
  {
    files: ['**/*.{ts,tsx}'],

    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
      parser: tseslint.parser,
      parserOptions: {
        ecmaFeatures: { jsx: true },
      },
    },

    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },

    rules: {
      ...reactHooks.configs['recommended-latest'].rules,
      ...reactRefresh.configs.vite.rules,
      // eigene Zusatzregeln kannst du hier ergänzen
    },
  },

  // Ganz am Ende: Prettier-Konfig – schaltet Formatierungsregeln von ESLint ab
  prettier,
]);
