import{t as e}from"./vendor-gsap-BMHLB6Su.js";function t(t,n={}){let{fromY:r=30,duration:i=.5,stagger:a=.06,ease:o=`power3.out`,delay:s=0,from:c=`start`}=n;return e.fromTo(t,{opacity:0,y:r},{opacity:1,y:0,duration:i,stagger:{each:a,from:c},ease:o,delay:s,clearProps:`transform`})}function n(t){let n=e.timeline();n.to(t,{scale:1.35,duration:.12,ease:`power2.out`}).to(t,{scale:1,duration:.25,ease:`elastic.out(1, 0.4)`});let r=t.getBoundingClientRect(),i=r.left+r.width/2,a=r.top+r.height/2;for(let t=0;t<8;t++){let t=document.createElement(`div`);t.style.cssText=`
      position: fixed;
      left: ${i}px;
      top: ${a}px;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #f05a28;
      pointer-events: none;
      z-index: 9999;
    `,document.body.appendChild(t),e.to(t,{x:e.utils.random(-40,40),y:e.utils.random(-50,-20),opacity:0,scale:0,duration:.6,ease:`power2.out`,onComplete:()=>t.remove()})}return n}function r(t){let n=e.timeline();return n.to(t,{scale:.6,duration:.1,ease:`power2.in`}).to(t,{scale:1.2,duration:.2,ease:`back.out(2)`}).to(t,{scale:1,duration:.15,ease:`power2.out`}),n}export{n,t as r,r as t};