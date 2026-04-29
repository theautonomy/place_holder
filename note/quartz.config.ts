import { QuartzConfig } from "./quartz/cfg"
import * as Plugin from "./quartz/plugins"

const config: QuartzConfig = {
  configuration: {
    pageTitle: "__TITLE__",
    pageTitleSuffix: "",
    enableSPA: true,
    enablePopovers: true,
    analytics: null,
    locale: "en-US",
    baseUrl: "__BASEURL__",
    ignorePatterns: ["private", "templates", ".obsidian"],
    defaultDateType: "modified",
    generateSocialImages: false,
    theme: {
      fontOrigin: "googleFonts",
      cdnCaching: true,
      typography: {
        header: "Inter",
        body: "Source Sans Pro",
        code: "JetBrains Mono",
      },
      colors: {
        // Tokyo Night — light variant (Tokyo Storm)
        lightMode: {
          light: "#e1e2e7",
          lightgray: "#c0caf5",
          gray: "#9aa5ce",
          darkgray: "#565f89",
          dark: "#1f2335",
          secondary: "#7aa2f7",
          tertiary: "#bb9af7",
          highlight: "rgba(122, 162, 247, 0.15)",
          textHighlight: "#e0af6888",
        },
        // Tokyo Night — dark variant
        darkMode: {
          light: "#1a1b26",
          lightgray: "#24283b",
          gray: "#414868",
          darkgray: "#a9b1d6",
          dark: "#c0caf5",
          secondary: "#7aa2f7",
          tertiary: "#bb9af7",
          highlight: "rgba(122, 162, 247, 0.15)",
          textHighlight: "#e0af6888",
        },
      },
    },
  },
  plugins: {
    transformers: [
      Plugin.FrontMatter(),
      Plugin.CreatedModifiedDate({
        priority: ["frontmatter", "filesystem"],
      }),
      Plugin.SyntaxHighlighting({
        theme: {
          light: "tokyo-night",
          dark: "tokyo-night",
        },
        keepBackground: false,
      }),
      Plugin.ObsidianFlavoredMarkdown({ enableInHtmlEmbed: false }),
      Plugin.GitHubFlavoredMarkdown(),
      Plugin.TableOfContents(),
      Plugin.CrawlLinks({ markdownLinkResolution: "shortest" }),
      Plugin.Description(),
      Plugin.Latex({ renderEngine: "katex" }),
    ],
    filters: [Plugin.RemoveDrafts()],
    emitters: [
      Plugin.AliasRedirects(),
      Plugin.ComponentResources(),
      Plugin.ContentPage(),
      Plugin.FolderPage(),
      Plugin.TagPage(),
      Plugin.ContentIndex({
        enableSiteMap: true,
        enableRSS: true,
      }),
      Plugin.Assets(),
      Plugin.Static(),
      Plugin.NotFoundPage(),
    ],
  },
}

export default config
