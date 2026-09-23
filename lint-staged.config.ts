import type {Configuration} from 'lint-staged';

export default ({
  '*': ((): string => 'secretlint "**/*"'),
  'site/**/*': ((): string[] => ['bun run --filter "@modkit/site" lint'])
} satisfies Configuration);
