import type {Theme} from 'vitepress';
import DefaultTheme from 'vitepress/theme';
import {h} from 'vue';
import './custom.scss';
import {gradleDslSync} from './gradle-dsl';
import NotFound from './NotFound.vue';
import PreReleaseNotice from './PreReleaseNotice.vue';

export default ({
  extends: DefaultTheme,
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type -- Would be messy to type.
  Layout: (() =>
    h(
      DefaultTheme.Layout,
      null,
      {
        'doc-before': (() => h(PreReleaseNotice)),
        'not-found': (() => h(NotFound))
      }
    )),
  enhanceApp: (({app, router}): void => {
    app.config.globalProperties.$modkitVersion = '0.10.0';

    // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition -- Intentional.
    if(globalThis.window === undefined) {
      return;
    }

    router.onAfterRouteChange = ((to: string): void => {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-type-assertion -- dataLayer is defined by GTM
      ;(globalThis as any).dataLayer?.push({
        event: 'page_view',
        page_path: to
      });
    });

    // Must run after the GTM assignment above because this wraps the current
    // `onAfterRouteChange`.
    gradleDslSync(router);
  })
} satisfies Theme);
