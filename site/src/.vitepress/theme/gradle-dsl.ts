import type {Router} from 'vitepress';

// Syncs every Gradle Kotlin/Groovy code group across the whole site.

const KEY = 'gradle-dsl';
const VALUES = [
  'Groovy',
  'Kotlin'
];

interface LabelElement extends HTMLLabelElement {
  textContent: string;
}

// eslint-disable-next-line @typescript-eslint/no-unsafe-type-assertion -- Correctly typed.
const groupLabels = ((group: Element): LabelElement[] => ([...group.querySelectorAll('.tabs label')] as LabelElement[]));

// A code group counts as a Gradle DSL switch only if it offers both Groovy and
// Kotlin tabs.
const isGradleGroup = ((group: Element): boolean => {
  const texts = (new Set(groupLabels(group).map(labelElem => labelElem.textContent.trim())));

  return VALUES.every(value => texts.has(value));
});

// VitePress moves the `.active` block in each group's own label click handler
// (keyed by tab index), so we replay a real click rather than just checking the
// radio. `applying` guards against the synthetic clicks re-entering our
// listener.
let applying = false;

const apply = ((preference: string): void => {
  applying = true;

  // Clicking a <label> focuses its radio input, and focusing an off-screen
  // input scrolls it into view. On pages with multiple Gradle groups the
  // synthetic clicks below would jump the viewport, so capture the scroll
  // position and restore it once we're done. Both happen synchronously before
  // the next paint, so there's no visible flash.
  const {scrollX, scrollY} = globalThis;

  try {
    for(const group of document.querySelectorAll('.vp-code-group')) {
      if(!isGradleGroup(group)) {
        continue;
      }

      const label = groupLabels(group).find(labelElem => (labelElem.textContent.trim() === preference));
      if(!label) {
        continue;
      }

      // eslint-disable-next-line @typescript-eslint/no-unsafe-type-assertion, unicorn/prefer-query-selector -- Correctly typed.
      const input = (document.getElementById(label.htmlFor) as (HTMLInputElement | null));
      if(input && !input.checked) {
        label.click();
      }
    }
  } finally {
    window.scrollTo(scrollX, scrollY);
    applying = false;
  }
});

const getPreference = ((): (string | null) => {
  const preference = localStorage.getItem(KEY);

  return ((preference && VALUES.includes(preference)) ? preference : null);
});

let installed = false;

// eslint-disable-next-line max-lines-per-function -- Clean.
const gradleDslSync = ((router: {onAfterRouteChange?: Router['onAfterRouteChange'];}): void => {
  const applyStored = ((): void => {
    const preference = getPreference();

    if(preference) {
      requestAnimationFrame(() => {
        apply(preference);
      });
    }
  });

  if(!installed) {
    installed = true;
    document.addEventListener(
      'click',
      ((e): void => {
        if(applying) {
          return;
        }

        // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion, @typescript-eslint/no-unsafe-type-assertion -- Correctly typed.
        const label = ((e.target as HTMLElement).closest('.vp-code-group .tabs label') as (LabelElement | null));
        if(!label) {
          return;
        }

        const group = label.closest('.vp-code-group');
        if(!group || !isGradleGroup(group)) {
          return;
        }

        const text = label.textContent.trim();
        if(!VALUES.includes(text)) {
          return;
        }

        localStorage.setItem(KEY, text);
        requestAnimationFrame(() => {
          apply(text);
        });
      })
    );
  }

  // Re-apply after every client-side navigation (content re-renders).
  const original = router.onAfterRouteChange;
  router.onAfterRouteChange = ((to: string): void => {
    original?.(to);
    applyStored();
  });
  applyStored();
});

export {
  gradleDslSync
};
