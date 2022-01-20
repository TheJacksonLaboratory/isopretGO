package org.jax.isopret.core.visualization;

public class HtmlUtil {


    public static final String header = """
            <!doctype html>
            <html class="no-js" lang="">
                        
            <head>
              <meta charset="utf-8">
              <meta http-equiv="x-ua-compatible" content="ie=edge">
              <title>Isopret: Isoform interpretation</title>
              <meta name="description" content="">
              <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                        
              <style>
                        
            * {
                -moz-box-sizing: border-box;
                -webkit-box-sizing: border-box;
                box-sizing: border-box
            }
                        
            html, body, h1, li, a, article, aside, footer, header, main, nav, section {
            	padding: 0;
            	margin: 0;
            }
                        
            html, body {
            	font-size:14px;
            }
                        
            body {
            	font-family:"DIN Next", Helvetica, Arial, sans-serif;
            	line-height:1.25;
            	background-color: white;
            }
                        
                        
            body > header, nav, main, body > section, footer {
            max-width:1200px;
            margin-left:auto;
            margin-right:auto;
            }
                        
            @media(min-width:1440px) {
            body > header, nav, main, body > section, footer {
                width:90%;
                max-width:unset;
                }
            }
                        
            main, body > section {
            	margin-top:1.5rem;
            	margin-bottom:1.5rem;
            }
                        
            body > header, body > section {
            	padding:2.1rem 2rem 1.6rem;
            }
                        
            .fr {
              float: right;
            }
                        
            a[href] {
            	color:#05396b;
            }
                        
            a[href]:hover {
            	color:#009ed0;
            }
                        
            p {
            	padding:0;
            	margin:0.75rem 0;
            }
                        
            h1 {
            	font-family:"DIN Next", Helvetica, Arial, sans-serif;
            	font-weight:700;
            	font-size:1.8rem;
            	line-height:1;
            }
                        
            .center {
              text-align: center;
            }
                        
            main > section > a[name="othergenes"] > h3,
            h2 {
            	font-family:"DIN Next", Helvetica, Arial, sans-serif;
            	font-weight:700;
            	font-size:1.5rem;
            	line-height:1;
            	margin:0 0 0.5rem;
            	padding:0;
            }
                        
            h3 {
            	font-family:"DIN Next", Helvetica, Arial, sans-serif;
            	font-weight:700;
            	font-size:1.2rem;
            	line-height:1;
            	margin:0 0 0.5rem;
            	padding:0;
            }
                        
                        
                        
            main ul, main ol {
            	margin:0.5rem 0 0.5rem 1.4rem;
            	padding:0;
            }
                        
            main li {
            	margin:0.25rem 0;
            	padding:0;
            }
                        
            .banner {
            	background-color: #05396b;
            	color: white;
            }
                        
            nav {
            	background-color: #4DA8DA;
            	margin-top:1px;
            	overflow:auto;
            	zoom:1;
            	padding:0;
            }
                        
            nav a[href] {
            	color:white;
            	text-decoration:none;
            	color:rgba(255,255,255,0.8);
            	font-size:1.2rem;
            	display:block;
            	padding:1rem;
            	font-weight:400;
            }
                        
            nav li:last-child a[href] {
            	padding-right:2.25rem;
            }
                        
            nav a[href]:hover {
            	color:#05396b;
            	background-color:#04c3ff;
            }
                        
            #navi ul {
            	display:table;
            	float:right;
            	margin:0;
            }
                        
            #navi li {
            	display:block;
            	float:left;
            }
                        
            main > section:first-child {
            	margin-top:1.5rem;
            	margin-bottom:1.5rem;
            	background-color:white;
            	padding:2.1rem 2rem 1.6rem;
            }
                        
            main > section {
            	margin-top:1.5rem;
            	margin-bottom:0;
            	background-color:white;
            	padding: .5rem;
            }
                        
            main > section > article {
            	padding: 1.5rem;
            	margin-top:1px;
            	background-color:white;
            }
                        
            table {
            	border-collapse: collapse;
            	width:100%;
            	margin:0.5rem 0;
            }
                        
            th, td {
            	text-align:left;
            	padding:0.4rem 0.5rem 0.25rem;
            }
                        
            th {
            	background-color: #e0e3ea;
            	border-bottom:1px solid white;
            }
                        
            
            gotable.th
             {
               vertical-align: bottom;
               text-align: center;
               border-collapse: collapse;
               border: 1px solid #000;
             }
             
             span
             {
               -ms-writing-mode: tb-rl;
               -webkit-writing-mode: vertical-rl;
               writing-mode: vertical-rl;
               transform: rotate(180deg);
               white-space: nowrap;
               padding: 5px 10px;
                margin: 0 auto;
             }
                       
                        
            .svgrow{
                width: 90%;
                min-height: 100px;
                margin: 0 auto;
                display: -webkit-flex; /* Safari */
                display: flex; /* Standard syntax */
            }
                        
            footer {
            	background-color: #05396b;
            	color: white;
            	padding: 1rem 2rem;
            }
                        
            /* The following links are in the SVG for the differentials */
            a.svg:link, a.svg:visited {
              cursor: pointer;
            }
                        
            a.svg text,
            text a.svg {
              fill: blue; /* Even for text, SVG uses fill over color */
              text-decoration: underline;
            }
                        
            a.svg:hover, a.svg:active {
              outline: dotted 1px blue;
            }
                        
            .features-title {
              background-color: #05396b;
              color: white;
            }
                        
            .features-title:nth-child(1) {
              border-right: 2px solid white;
            }
                        
            .features-data {
              background-color: #e0e3ea;
            }
                        
            .features-data:nth-child(1) {
              border-right: 2px solid white;
            }
            .no-list-style {
              list-style-type: none;
            }
                        
            .tooltip {
              position: relative;
              display: inline-block;
              border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
              outline: none;
              width: 150px;
              min-width: 150px;
              max-width: 250px;
            }
                        
            /* Tooltip text */
            .tooltip .tooltiptext {
              visibility: hidden;
              background-color: black;
              color: #fff;
              text-align: left;
              padding: 5px 0;
              border-radius: 6px;
              /* Position the tooltip text - see examples below! */
              position: absolute;
              z-index: 1;
            }
                        
            /* Show the tooltip text when you mouse over the tooltip container */
            .tooltip:hover .tooltiptext {
              visibility: visible;
            }
                        
            .table-btn {
                display: block;
                font-weight: bold;
                padding: 10px;
                background-color: #05396b;
                width: fit-content;
                color: white;
                cursor: pointer;
            }
   
            .badge{
              font:  Arial, Helvetica, sans-serif;
              font-size: x-large;
              font-weight: bolder;
              line-height: 1;
              color: #f4f4f4;
              background-color: #214fe0;
              padding:0.25rem;
            }
            </style>
            </head>
            """;

    public static final String htmlTop = """
            <body>
              <!--[if lte IE 9]>
                <p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="https://browsehappy.com/">upgrade your browser</a> to improve your experience and security.</p>
              <![endif]-->
            <header class="banner">
                <h1><font color="#FFDA1A">Isopret</font></h1>
            </header>
            <main>
            """;


    public static final String intro = """
            """;

    public static final String bottom = """
           <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
           </main>
           <footer>
               <p>Isopret &copy; 2022</p>
           </footer>
            <script>
              function showTooltip(evt, text) {
                let tooltip = document.getElementById("tooltip");
                tooltip.innerText = text;
                tooltip.style.display = "block";
                tooltip.style.left = evt.pageX + 10 + 'px';
                tooltip.style.top = evt.pageY + 10 + 'px';
              }
                        
              function hideTooltip() {
                var tooltip = document.getElementById("tooltip");
                tooltip.style.display = "none";
              }
            </script>
            </body>
            </html>
            """;


    public static String wrap(String html) {
        return header + htmlTop +  html + bottom;
    }






}
