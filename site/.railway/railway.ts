import {defineRailway, github, project, service} from 'railway/iac';

export default defineRailway(() => {
  const web = service(
    'web',
    {
      source: github(
        'oliveryasuna/modkit',
        {
          branch: '23-move-docs-into-this-repo',
          checkSuites: false
        }
      ),
      replicas: {'us-east4-eqdc4a': 1},
      build: {
        builder: 'RAILPACK',
        buildCommand: 'bunx --no-install run --filter "@modkit/site" build',
        watchPatterns: [
          'site/.railway/**/*',
          'site/src/**/*',
          'bun.lock'
        ]
      },
      env: {RAILPACK_SPA_OUTPUT_DIR: 'site/dist'}
    }
  );

  return project(
    'docs',
    {resources: [web]}
  );
});
