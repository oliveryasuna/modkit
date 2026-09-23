import {defineConfig} from '@oliveryasuna/eslint-config';
import packageJson from './package.json' assert {type: 'json'};

const NODE_ENGINE = packageJson.engines.node;

export default defineConfig(
  {
    ignores: (defaults => [
      ...defaults,
      'README.md',
      '*git-ignore*',
      'packages/**/*'
    ]),
    javascript: {
      globals: {
        Bun: 'readonly',
        NodeJS: 'readonly'
      }
    },
    jsdoc: {
      overrides: {
        'jsdoc/check-line-alignment': ['error', 'always'],
        'jsdoc/tag-lines': 'off'
      }
    },
    typescript: {
      typeAware: true,
      tsconfigRootDir: import.meta.dirname,
      overrides: {
        '@typescript-eslint/method-signature-style': [
          'error',
          'method'
        ]
      }
    }
  },
  {
    rules: {
      '@stylistic/object-property-newline': ['error', {allowAllPropertiesOnSameLine: true}],
      'n/no-unsupported-features/node-builtins': [
        'error',
        {
          version: NODE_ENGINE,
          ignores: [
            'import.meta.dirname',
            'localStorage'
          ]
        }
      ]
    }
  }
);
