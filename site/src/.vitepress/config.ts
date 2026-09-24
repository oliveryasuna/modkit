import {defineConfig} from 'vitepress';

// TODO: Add GTM ID.
const GTM_ID = 'TODO';

export default defineConfig({
  lang: 'en-US',
  title: 'Modkit',
  // eslint-disable-next-line @stylistic/max-len -- Description is long.
  description: 'Modkit is a suite of Gradle plugins that simplifies the tooling for Minecraft mod development. You describe your mod once, including its identity, target versions, dependencies, and metadata, and Modkit automatically generates builds for multiple loaders (such as Fabric and NeoForge) based on that single description.',

  cleanUrls: true,

  // markdown: {lineNumbers: true},

  themeConfig: {
    logo: {
      light: '/modkit-logo-light.svg',
      dark: '/modkit-logo-dark.svg'
    },

    notFound: {quote: 'The page you were looking for fell into lava. It\'s contents could not be recovered.'},

    search: {provider: 'local'},

    nav: [
      {
        text: 'Home',
        link: '/'
      },
      {
        text: 'Docs',
        link: '/overview'
      }
    ],

    sidebar: {
      '/': [
        {
          text: 'Modkit',
          items: [
            {
              text: 'Overview',
              link: '/overview'
            },
            {
              text: 'Introduction',
              link: '/introduction'
            },
            {
              text: 'Getting started',
              link: '/getting-started'
            }
          ]
        },
        {
          text: 'Concepts',
          collapsed: false,
          items: [
            {
              text: 'The Modkit model',
              link: '/concepts/the-model'
            },
            {
              text: 'The plugin suite',
              link: '/concepts/the-plugin-suite'
            },
            {
              text: 'Multi-version builds',
              link: '/concepts/multi-version'
            }
          ]
        },
        {
          text: 'Guides',
          collapsed: false,
          items: [
            {
              text: 'Mod metadata',
              link: '/guides/metadata'
            },
            {
              text: 'Mixins',
              link: '/guides/mixins'
            },
            {
              text: 'Dependencies',
              link: '/guides/dependencies'
            },
            {
              text: 'Run configurations',
              link: '/guides/runs'
            },
            {
              text: 'Data generation',
              link: '/guides/datagen'
            },
            {
              text: 'Publishing',
              link: '/guides/publishing'
            },
            {
              text: 'Continuous integration',
              link: '/guides/ci'
            },
            {
              text: 'Testing',
              link: '/guides/testing'
            },
            {
              text: 'Multi-version',
              link: '/guides/multi-version'
            }
          ]
        },
        {
          text: 'Reference',
          collapsed: false,
          items: [
            {
              text: 'modkit { } (core)',
              link: '/reference/core'
            },
            {
              text: 'Loaders',
              link: '/reference/loaders'
            },
            {
              text: 'Metadata',
              link: '/reference/metadata'
            },
            {
              text: 'Mixins',
              link: '/reference/mixins'
            },
            {
              text: 'Dependencies',
              link: '/reference/dependencies'
            },
            {
              text: 'Run',
              link: '/reference/run'
            },
            {
              text: 'Datagen',
              link: '/reference/datagen'
            },
            {
              text: 'Publish',
              link: '/reference/publish'
            },
            {
              text: 'CI',
              link: '/reference/ci'
            },
            {
              text: 'Testing',
              link: '/reference/testing'
            },
            {
              text: 'Multiversion',
              link: '/reference/multiversion'
            },
            {
              text: 'Scaffold',
              link: '/reference/scaffold'
            }
          ]
        }
      ]
    },

    socialLinks: [
      {
        icon: 'github',
        link: 'https://github.com/oliveryasuna/modkit'
      },
      {
        icon: 'discord',
        link: 'https://discord.gg/WzcXYYbcr7'
      }
    ],

    footer: {
      message: 'NOT AN OFFICIAL MINECRAFT WEBSITE. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.',
      copyright: `Copyright © ${(new Date()).getFullYear()} Oliver Yasuna`
    }
  },

  head: [
    [
      'script',
      {id: 'gtm'},
      `(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','${GTM_ID}');`
    ]
  ],

  transformHtml: ((code: string): string =>
    code.replace(
      /<body([^>]*)>/,
      `<body$1>
<noscript><iframe src="https://www.googletagmanager.com/ns.html?id=${GTM_ID}"
height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>`
    )),

  outDir: '../dist'
});
